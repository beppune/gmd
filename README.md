
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

