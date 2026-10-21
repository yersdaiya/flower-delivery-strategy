markdown
# Flower Delivery: Facade & Strategy Patterns (Assignments 4 & 5)

---

## Part 1: Facade Pattern over a Three-Stage Chain (Assignment 4)

### The Domain
A robust flower delivery service managing inventory, payment processing, and delivery logistics with durable SQLite state storage.

### Architecture & Subsystems
- **Inventory Subsystem:** Manages flower stock and availability (`inventory` package).
- **Payment Subsystem:** Handles secure payment transactions (`payment` package).
- **Delivery Subsystem:** Manages dispatch and delivery routes (`delivery` package).
- **Facade (`FlowerDeliveryFacade`):** A single unified entry point hiding subsystem complexities from the client.

---

## Part 2: Strategy Pattern Implementation (Assignment 5)

### 1. Classes and Responsibilities (SRP)

| Class Name | Responsibility |
| :--- | :--- |
| `PricingStrategy` | Interface defining calculation method. |
| `StandardPricingStrategy` | Calculates standard delivery cost. |
| `ExpressPricingStrategy` | Computes urgent delivery pricing. |
| `HolidayPricingStrategy` | Evaluates surge holiday pricing dynamically. |
| `DeliveryPricingContext` | Executes chosen pricing strategy. |
| `DeliverySelector` | Selects pricing strategy automatically based on rules. |
| `ExternalFactorProvider` | Isolates external pricing factor source. |
| `FakeFactorProvider` | Simulates external pricing factor data for testing. |
| `PackagingStrategy` | Interface defining bouquet presentation. |
| `EcoPackaging` | Computes eco-friendly packaging fee. |
| `LuxuryPackaging` | Computes premium gift box fee. |
| `MinimalistPackaging` | Computes transparent wrap fee. |
| `PackagingContext` | Executes chosen packaging strategy. |

### 2. Why Strategy Pattern (not State or Template Method)?
The **Strategy Pattern** is chosen because our algorithms (pricing and packaging) are completely interchangeable at runtime based on external rules and user choice. Unlike the *Template Method*, which defines a skeleton of an algorithm in a base class using inheritance, Strategy encapsulates family algorithms behind a clean interface favoring composition. Unlike the *State Pattern*, where an object alters its behavior when its internal state changes and states transition from one to another autonomously, Strategy instances are independent, stateless, and explicitly chosen or injected by clients or selectors depending on external conditions.

### 3. Refactoring Git Commits
- **Before Refactoring (Bad Way with switch-case):** [View Commit](https://github.com/yersdaiya/flower-delivery-strategy/commit/3281d48)
- **After Refactoring (Strategy Pattern Applied):** [View Commit History](https://github.com/yersdaiya/flower-delivery-strategy/commits/main/)
