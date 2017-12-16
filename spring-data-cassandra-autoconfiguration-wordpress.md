I received a few comments on my repository for [Reactive Streams with Spring Data Cassandra](https://lankydanblog.com/2017/12/11/reactive-streams-with-spring-data-cassandra/) regarding configuration that was not required. This was due to me not making use of Spring Boot's auto-configuration which would of allowed me to remove a whole class from my code! Therefore, in an attempt to redeem myself for not taking advantage of auto-configuration I decided to write a post about what it does for you and how to use it in conjunction with Spring Data Cassandra. 

I will provide you with a warning before we begin, there are no code examples in this post because, well, Spring Boot does it all for us.

So let's get on with it.

As always if you do not have any current knowledge on Spring Data Cassandra I recommend you read my first post on the subject, [Getting started with Spring Data Cassandra](https://lankydanblog.com/2017/10/12/getting-started-with-spring-data-cassandra/).

Auto-configuration allows Spring to detect any beans and annotations that have not been included in our code and automatically add them for us. For it to kick in we must have one of the classes defined in <code>@ConditionalOnClass</code> in the classpath or a property specified in the <code>@ConditionalOnProperty</code> annotation. Because of this, all the beans we need to connect to Cassandra are automatically created just by including the <code>spring-boot-starter-data-cassandra</code> or <code>spring-boot-starter-data-cassandra-reactive</code> dependency and enabling auto-configuration. That's it. But if we find any reason to define some of the beans that are normally created by auto-configuration then there is no reason to worry. Thanks to the <code>@ConditionalOnMissingBean</code> annotation added to each auto-configured bean preventing another being created if one already exists in the <code>BeanFactory</code>.

The main classes that deal with Spring Data Cassandra's auto-configuration are:
 
- <code>CassandraRepositoriesAutoConfiguration</code> - enables auto-configuration for Cassandra repositories.
- <code>CassandraRepositoriesAutoConfigureRegistrar</code> applies <code>@EnableCassandraRepositories</code>. 
- <code>CassandraDataAutoConfiguration</code> - creates beans revolving around a session such as <code>Session</code> and <code>CassandraTemplate</code>.
- <code>CassandraAutoConfiguration</code> creates a Cassandra <code>Cluster</code>.
 
 I recommend checking out these classes if you want to know exactly what properties they are setting. All of the properties set on beans in the auto-configuration classes are also set when you extend <code>AbstractCassandraConfiguration</code> but doing so also comes with a few extras, such as startup and shutdown scripts.

There are also reactive versions of each of the above auto-configuration classes bar <code>CassandraAutoConfiguration</code> as it only creates a cluster which does not need a reactive implementation. Therefore if we stick to using auto-configuration we will have all the beans we need to query data normally as well as reactively using Reactive Streams.

Inside these classes you will find the <code>CassandraProperties</code> class. This uses the <code>@ConfigurationProperties</code> annotation to pickup all Cassandra configuration that is prefixed with <code>spring.data.cassandra</code>. This means that we can control the application solely from a properties file if we are fine using the auto-configured beans. <code>CassandraProperties</code> contains a lot of properties inside of it, I haven't gone through each one but I think it's is safe to assume that most properties that a <code>Session</code> and <code>Cluster</code> need can be found there.

Remember that we must specify the keyspace or everything goes boom! Before, we would do this in a configuration class that extended <code>AbstractCassandraConfiguration</code> or <code>AbstractReactiveCassandraConfiguration</code> but because we are using auto-configuration we are going to add it to <code>application.properties</code> or whatever you want to call your properties file. For convenience I also added another property that will create tables based on the entities.

[gist https://gist.github.com/lankydan/c393c7ffcb2facc6bf4ed6ea776309b6 /]

If you want to change the contact points and port your application uses then you will need to add the following properties.

[gist https://gist.github.com/lankydan/5913929272574b6e9b99d78b916b41a4 /]

One quick thing about the names of the properties. This isn't restricted to Spring Data Cassandra but I thought it is worth adding. The way you format or case your properties does not really matter so you can choose how you write it. Below are a few different ways you could write them.

[gist https://gist.github.com/lankydan/b94dadd521e1b45a8cbe51d9e3f3263c /]

I personally don't think you should choose the last one but it will still work!

The last thing that we need to do is actually enable auto-configuration by using either <code>@SpringBootApplication</code> or <code>@EnableAutoConfiguration</code> on one of the configuration classes. In my example I used <code>@SpringBootApplication</code> on my main <code>Application</code> class.

And thats about it really. I don't think there is much to gain by showing your the rest of the code but if you really want to see it all it is on my [GitHub](https://github.com/lankydan/spring-data-cassandra/tree/spring-data-cassandra-autoconfiguration).

In conclusion, Spring Boot's auto-configuration allows us to write way less code if we are happy with magic that it does behind the scenes. Some people do not like using auto-configuration because of this but hopefully I have pulled back the certain slightly and allowed you to see what it is doing. If you do decide to use auto-configuration then you can start querying Cassandra with very little setup, just write some entities and their Cassandra repositories and you good to go. This not only helps you finish your code faster (so you can get back to Reddit) but also gives you a bit of a safety net in case you forget to create a bean you need. That being said, at the end of the day it is your decision whether you use it or not. If you don't like Spring Boot doing everything for you then you can set it all up yourself and sleep easy knowing that you have complete control over your application.

But I'm going to keep being lazy and use auto-configuration so I can type less.

As mentioned earlier the rest of the code not shown here can be found on my [GitHub](https://github.com/lankydan/spring-data-cassandra/tree/spring-data-cassandra-autoconfiguration).