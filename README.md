
### Production build command

```mvn -Dvaadin.force.production.build=true -Pproduction clean package```

The `-Pproduction` profile option is required because this project has been created
using Spring Initializr.

### Some .npmrc useful parameters
```
proxy=http://localhost:3128
https-proxy=http://localhost:3128
noproxy=localhost, 127.0.0.1,10.98.2.*
```

### Fix the diff assertEquals not showing in IntelliJ
#### Issue: Missing “View assertEquals Difference” in IntelliJ.
While running tests with JUnit 5 in IntelliJ IDEA (version 2025.3.3), the IDE did not display the string comparison diff for failing assertions using:

`kotlin.test.assertEquals(expected, actual)`

Even though:

* Tests were executed using the IntelliJ runner (not Maven)
* The arguments were String
* The parameter order was correct (expected, then actual)

The problem was that IntelliJ did not recognize kotlin.test.assertEquals as a diff-supported assertion in this version. The Kotlin assertion function is a wrapper abstraction, and the IDE’s diff viewer currently detects only specific assertion implementations directly (not all wrappers).

#### Solution

The issue was resolved by replacing:

`import kotlin.test.assertEquals`

with:

`import org.junit.jupiter.api.Assertions.assertEquals`

Using the direct JUnit Jupiter assertion allows IntelliJ to correctly detect the expected/actual comparison and display the side-by-side diff view for failing string assertions.
This change does not affect test behavior, only IDE diff rendering.
