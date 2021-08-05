# Fixture Monkey

### "Write once, Test anywhere"

Enjoy your test with Fixture Monkey.

You can write countless tests including edge cases by `only one fixture`.

See the [wiki](../../wiki) for further details and documentation.

## Example

```java

@Data   // lombok getter, setter
public class Order {
    @NotNull
    private Long id;

    @NotBlank
    private String orderNo;

    @Size(min = 2, max = 10)
    private String productName;

    @Min(1)
    @Max(100)
    private int quantity;

    @Min(0)
    private long price;

    @Size(max = 3)
    private List<@NotBlank @Size(max = 10) String> items = new ArrayList<>();

    @PastOrPresent
    private Instant orderedAt;

    @Email
    private String sellerEmail;
}

    @Test
    void test() {
        // given
        FixtureMonkey sut = FixtureMonkey.builder().build();

        // when
        Order actual = sut.giveMeOne(Order.class);

        // then
        then(actual.getId()).isNotNull();
    }
```

## Requirements

* JDK 1.8 or higher

## Install

### Gradle

```groovy
testImplementation("com.navercorp.fixturemonkey:fixture-monkey:0.2.4")
```

### Maven

```xml

<dependency>
    <groupId>com.navercorp.fixturemonkey</groupId>
    <artifactId>fixture-monkey</artifactId>
    <version>0.2.4</version>
</dependency>
```

## Submodule

* fixture-monkey-jackson
* fixture-monkey-kotlin
* fixture-monkey-autoparams
* fixture-monkey-mockito
