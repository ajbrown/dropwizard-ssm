[![Build Status](https://travis-ci.org/ajbrown/dropwizard-ssm.svg?branch=master)](https://travis-ci.org/ajbrown/dropwizard-ssm)

Dropwizard SSM allows [AWS's Simple System Management parameters](https://docs.aws.amazon.com/systems-manager/latest/userguide/systems-manager-paramstore.html) parameters to be used for variable substitution within Dropwizard 
configurations.  

### Why Dropwizard SSM
A common way to provide environment specific configuration values to your Dropwizard application
is to use the built-in `EnvironmentVariableSubstitutor`, which will replace variables like `${MY_ENV_VAR}` with a value
loaded from the environment through `System.getenv("MY_ENV_VAR"")`.  This is convenient, but requires you to update the 
environment variables on the machine running Dropwizard whenever you want to change the value.  That's easy for something 
like Docker containers, but doesn't work very well for virtual machines provisioned in a more traditional fashion, and
definintely not for bare metal.

Furthermore, this is a security nightmare for sensitive data like database passwords.  Writing those files out to the 
filesystem or passing them in your launch means anyone that has access to the environment also knows the credentials.  

By using Simple Systems Management parameter stores, we can store configuration values in a centralized location, using
KMS to encrypt values where needed, and IAM permissions to control who has access to those parameters.  When we want to
update a configuration value, we just need to update it in one place -- no need to update the runtime environment on
your servers! 


> **Note**: At this time, this library does not provide a way to dynamically update configuration values at runtime.  
> Variable substitution happens on startup, so the configuration class will contain the value that was there when the 
> application starts.  Unfortunately, this means you still need to restart your service whenever a critical value changes. 

### Usage Example

Similar to environment variable substitution, the variables in your configuration file should match the fully-qualified
name of the paramter that contains the value.  

Add the Maven dependency:
```xml
  <dependency>
    <groupId>org.ajbrown.dropwizard</group>
    <artifactId>dropwizard-ssm</artifactId>
    <version>1.0</version>
  </dependency>
```

Add fully-qualified parameter names to your configuration:

```yaml
database:
  driverClass: com.mysql.cj.jdbc.Driver
  url: ${JDBC_URL:-jdbc:mysql://localhost/my-database}
  user: ${/myapp/database-user}
  password: ${/myapp/database-user}
```

Register SSM configuration variable substitution:

```java
    // Enable variable substitution with AWS Simple Systems Management
    bootstrap.setConfigurationSourceProvider(
        new SubstitutingSourceProvider(bootstrap.getConfigurationSourceProvider(),
            new SsmSubstitutor(false)
        )
    );
```

#### Using With Environment Variable Substitution 

You can also use this substitutor (and any other well-built substitutor) alongside the built in environment variable 
substitutor. In my configuration above, I'm actually expecting that the JDBC_URL is provided by an environment variable,
while the username and passwords are provided by a parameter store.  You can chain substitutors together to accomplish 
this.


```java
    // Enable variable substitution with environment variables
    bootstrap.setConfigurationSourceProvider(
        new SubstitutingSourceProvider(bootstrap.getConfigurationSourceProvider(),
            new EnvironmentVariableSubstitutor(false)
        )
    );


    // Enable variable substitution with AWS Simple Systems Management
    bootstrap.setConfigurationSourceProvider(
        new SubstitutingSourceProvider(bootstrap.getConfigurationSourceProvider(),
            new SsmSubstitutor(false)
        )
    );
```

#### Using Namespace Prefixes

Often, you want to use the same configuration file across multiple environments, but have different values substituted 
depending on the environment.  To support this, you can set a "namesspace prefix" during configuration.  When provided, 
Dropwizard SSM will add this prefix to all parameter names it uses. 

```java
    bootstrap.setConfigurationSourceProvider(
        new SubstitutingSourceProvider(bootstrap.getConfigurationSourceProvider(),
            new SsmSubstitutor(false, System.getenv("ENV") + "/")
        )
    );

```
