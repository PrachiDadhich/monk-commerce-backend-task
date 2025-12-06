# Coupon Management API (In-memory H2)

## Overview
**The primary goal of this task was to design a flexible, extensible, real-world coupon engine, capable of handling multiple coupon types, constraints, and dynamic discount calculation.
The emphasis is not just on implementation but on thinking through as many relevant scenarios, edge cases, and future extensions as possible.**
---

### Below is a comprehensive list of use cases, grouped into:

* **Implemented Use Cases**
* **Considered But Not Implemented (Time Constraint / Out of Scope)**
* **Assumptions**
* **Limitations**
* **Design Decisions & Architecture Notes**
* **CRUD Support**
* **Extensibility**
* **Performance Considerations**
* **Conclusion**

---

### Requirements: Java 17+, Maven

### H2 Console
- URL: [http://localhost:8080/h2-console](http://localhost:8080/h2-console)  
- JDBC URL: `jdbc:h2:mem:coupondb`

### Swagger link
- URL: http://localhost:8080/swagger-ui/index.html

### Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/coupons` | Create a coupon (body contains `type` and `detailsJson`, optional `expiresAt`, `name`) |
| GET | `/coupons` | List all coupons |
| GET | `/coupons/{id}` | Get a specific coupon by ID |
| PUT | `/coupons/{id}` | Update a coupon |
| DELETE | `/coupons/{id}` | Delete a coupon |
| POST | `/applicable-coupons` | Returns all applicable coupons (with computed discount amounts) for a given cart |
| POST | `/apply-coupon/{id}` | Applies a coupon to the cart and returns updated cart (per-item `total_discount`), `total_price`, `total_discount`, `final_price` |

---

## Sample Payload

### Cart-wise Coupon:
```json
{
  "type": "CART_WISE",
  "detailsJson": {
    "threshold": 100,
    "discount": 10
  },
  "expiresAt": "2025-12-31",
  "name": "10% OFF above 100"
}
```

### Product-wise Coupon:
```json
{
  "type": "PRODUCT_WISE",
  "detailsJson": {
    "product_id": 1,
    "discount": 20
  },
  "expiresAt": null,
  "name": "20% off on Product 1"
}

```        

### BxGy Coupon:
```json
{
  "type": "BXGY",
  "detailsJson": {
    "buy_products": [1, 2,3,4],
    "get_products": [5, 6],
    "cutoff": 4,
    "repetition_limit": 2
  },
  "expiresAt": null,
  "name": "Buy X of [1,2] Get Y of [5] free"
}

```
---
## I. Implemented Cases

- Cart-wise percent discounts above a threshold.
- Product-wise percent discounts for a specified product ID.
- BxGy:
    - Buy-products are pooled.
    - Repetition limit enforced.
    - Discount is equal to price × quantity of get-products available in cart.
- Coupon expiration check (if expiresAt provided).
- Basic CRUD operations for coupons.

---

## II. Use Cases Considered (But Not Implemented Fully due to Time)

### 1. Stackable Coupons

- Applying multiple coupons together
- Handling order of application
- Handling conflicts between coupons

### 2. Auto-Selecting Best Coupon

- System chooses the highest discount automatically

### 3. Time-based Coupon Constraints

- Hour-of-day restrictions
- Weekend-only coupons
- Flash sale time windows

### 4. User-Specific Coupons

- First-order coupon
- New user only
- Loyalty-tiered discounts
- Region-based coupon

### 5. Inventory Aware BxGy

- Check stock availability of free items

### 6. Coupons with Upper/Lower Discount Limits

- Max discount amount
- Min cart value

*Although these were considered, they were intentionally not implemented due to scope and time.*

---

## III. Assumptions

- detailsJson contains a valid JSON structure depending on coupon type.
- Product IDs in the cart directly correspond to product IDs in coupon details.
- Currency rounding: amounts rounded to 2 decimal places.
- BxGy pooling: buy_products are summed to determine how many repeats the cart qualifies for (simpler logic).
- apply will mutate per-item totalDiscount and compute final totals from item subtotal - totalDiscount.
- Prices sent in cart are already validated and final (no backend pricing engine).
- The coupon calculation is stateless and depends only on:
  - CartRequest
  - Coupon definition from DB
- CartItem in input does not contain discount values — discounts are computed and returned only in output.
- One coupon is applied at a time.

--- 

## IV. Limitations

- Does not support applying multiple coupons at once.
- No logic for resolving coupon conflicts.
- Does not support null-safe or fuzzy matching for product IDs.
- No “cheapest item free” or sorting logic built into BxGy.
- No distributed cache/queue system for massive scale traffic.
- Reliance on client-passed price for calculations (not secure in full production).

---

## V. Design & Architecture Decisions

### 1. Strategy Pattern
- Keeps each coupon type isolated
- Adding new coupon types requires:
  - New strategy class
  - Factory entry
- Zero modification in service layer

### 2. Factory Pattern

Ensures clean selection of strategies:
```java
CouponStrategy strat = factory.getStrategy(coupon);
```

### 3. Immutable DTOs

- Safer
- Easier to debug
- Avoids accidental mutation
- Encourages pure functional transformations

### 4. Flexible detailsJson

- Stores coupon rules
- Easily extendable without DB schema changes

### 5. Separation of Concerns

- Service handles orchestration
- Strategy handles business logic
- Repository handles persistence
- DTOs handle API contract

---

## VI. CRUD Implementation

- **Create Coupon**

  Accepts request → validates → saves DB record with detailsJson
- **Update Coupon**

  Partial or full update of coupon fields

- **Delete Coupon**

  Soft delete not implemented (hard delete only)

- **Get Coupon(s)**

  Return all or by ID

### Coupon data stored includes:

- type
- detailsJson (variable rules per type)
- expiry date
- name

### Discount Application Logic:

**Flow for applying coupon:**
1. Fetch coupon by ID
2. Validate expiration
3. StrategyFactory returns correct strategy
4. isApplicable(cart, coupon)
5. Strategy applies discount and returns updated cart
6. Service calculates:
   - totalPrice
   - totalDiscount
   - finalPrice
7. Response returned to client

**This ensures consistency and extensibility.**

---

## VII. Extensibility

**The system can be extended easily to support:**
- New coupon types
- New validation rules
- Additional discount conditions
- Extended coupon metadata
- Multi-coupon application
- Customer segmentation rules
- Time windows, location-based rules
- Category-level logic

All without modifying existing strategies.

---

## VIII. Performance Considerations

- All strategy operations are O(n) over cart items
- No nested heavy loops
- Efficient map lookups
- Factory reduces branching complexity
- Jackson parsing used only once per coupon application
- No redundant DB calls

For larger scale:
- Could introduce caching
- Precompiled JSON → Java mappings
- Coupon pre-indexing per product/category
- Async coupon evaluation
- Distributed coupon cache (Redis)

---

## IX. Conclusion

This project successfully implements a scalable, maintainable, extensible coupon engine, covering real-world coupon types (Cart-wise, Product-wise, BxGy) and flexible future extensions.

Thoughtful consideration has been given to:
- Edge cases
- Coupon subtypes
- System design patterns
- Low coupling and high cohesion
- Extensibility and performance

While not every possible coupon scenario was implemented, the foundation is strong and ready for expansion.

---