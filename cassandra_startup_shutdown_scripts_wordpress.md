Shorter post this time round, it feels nice to get something finished faster than 2 weeks for once. Today we will look at startup and shutdown scripts in Spring Data Cassandra. This is something I probably should of done myself ages ago as it would of made testing my earlier posts much easier. I spent so much time (slightly over exaggerated) constantly truncating tables between each execution which was pretty annoying.

The content in this post is related to my earlier posts on Spring Data Cassandra, but does not directly require them to be read. That being said, it is worth looking at <a href="https://lankydanblog.com/2017/10/12/getting-started-with-spring-data-cassandra/" target="_blank" rel="noopener">Getting started with Spring Data Cassandra</a> for a basic understanding of the subject. The dependencies can also be found there.

Anyway, lets get started before the introduction is longer than the actual post.

There are two ways of writing startup and shutdown scripts, either by using the query builders tailor made for this situation or by CQL directly. The one you choose is up to you and I don't think either one is much better than the other, because at the end of the day theres not a ton that they need to do. Assuming you are using the <code>@Table</code> annotation on your entities and have <code>SchemaAction.CREATE_IF_NOT_EXITS</code> chosen in your configuration then your scripts will be even shorter as you don't need to think about creating any tables.

Before we start looking at the code we should first understand what creating a keyspace consists of.
<ul>
	<li>The keyspace name.</li>
	<li><code>IF NOT EXISTS</code> will only attempt to create the keyspace if it does not already exist when this statement is added.</li>
	<li>Replication strategy
    <ul>
      <li><code>SimpleStrategy</code> assigns the same replication factor to the entire cluster. This should be used for testing or local development environments where the way that data is replicated is not the primary concern.
      <pre>CREATE KEYSPACE myKeyspace WITH REPLICATION = {'class': 'SimpleStrategy', 'replication_factor': 1};</pre>
      </li>
	    <li><code>NetworkTopolgyStrategy</code> assigns specific replication factors to each data center defined within a comma separated list. This should be used in production environments.
      <pre>CREATE KEYSPACE myKeyspace WITH REPLICATION = {'class': 'NetworkTopolgyStrategy', 'datacenter_1': 1, 'datacenter_2': 2};</pre>
      </li>
    </ul>
  </li>
	<li><code>DURABLE_WRITES</code> specifies if the commit log is skipped when writing to the database. If <code>false</code> the commit log will be bypassed and when <code>true</code> writes will be sent there first ensuring that eventually all writes are persisted in the case of any network issues. Durable writes should never be set to <code>false</code> when using <code>SimpleStrategy</code> replication. This property is optional and will default to <code>true</code> if not set.
  <pre>CREATE KEYSPACE myKeyspace WITH REPLICATION = {'class':'SimpleStrategy', 'replication_factor': 1} AND DURABLE_WRITES = false;</pre>
  </li>
</ul>
Now that we have looked through what goes into creating a keyspace we can look at the code that does so.

[gist https://gist.github.com/lankydan/eb8d121248e12b27051110fecca23955 /]

Here we have <code>CassandraConfig</code> which does general configuration for cassandra. The methods we are overriding today though are actually found in <code>AbstractClusterConfiguration</code> which is extended by <code>AbstractCassandraConfiguration</code>. The rest of the Cassandra configuration has been hidden as it is not directly relevant to this post (this is all covered in <a href="https://lankydanblog.com/2017/10/12/getting-started-with-spring-data-cassandra/" target="_blank" rel="noopener">Getting started with Spring Data Cassandra</a>).

By extending <code>AbstractCassandraConfiguration</code> (or <code>AbstractClusterConfiguration</code>) we are able to override <code>getKeyspaceCreations</code> and <code>getKeyspaceDrops</code> which will be run at startup and shutdown. As the method names suggest these will create and drop keyspaces. By creating and dropping a table we are effectively truncating the table which can be helpful when testing your code.

<code>CreateKeyspaceSpecification</code> provides methods to create a statement that will be converted to CQL and execute a query similar to the CQL shown earlier. The CQL generated for the above example would be:
<pre>CREATE KEYSPACE myKeyspace IF NOT EXISTS WITH REPLICATION = {'class': 'SimpleStrategy', 'replication_factor': 1} AND DURABLE_WRITES = true;
</pre>
<code>DropKeyspaceSpecification</code> is even easier, all it does is drop a keyspace. Either by a <code>String</code> or <code>KeyspaceIdentifier</code> name. The CQL generated would be:
<pre>DROP KEYSPACE myKeyspace;
</pre>
See, nice and easy.

I mentioned earlier you could write the CQL directly, below is what that would look like.

[gist https://gist.github.com/lankydan/f425ad693a92d9743e5817b182d490c1 /]

The CQL in this example is the nearly same as the generated CQL from the earlier ones. The benefit of using <code>getStartupScripts</code> and <code>getShutdownScripts</code> is that you are able to do different operations such as <code>TRUNCATE</code>. If you are not creating tables based on your entities you won't want to drop your keyspace as you will need to run some script to recreate them, although the creation of the tables could be added to <code>getStartupScripts</code> to counteract this.

There are a few more important pieces of information I need to tell you. The shutdown scripts defined in <code>getShutdownScripts</code> are executed by the <code>destroy</code> method of the <code>CassandraClusterFactoryBean</code> and <code>CassandraCqlSessionFactoryBean</code> and the keyspaces dropped in <code>getKeyspaceDrops</code> are executed from the <code>destroy</code> method of only the <code>CassandraClusterFactoryBean</code> (we will see why this is important in a minute). Therefore to trigger them your application must end normally and the application context must call it's <code>close</code> method (found on <code>AbstractApplicationContext</code>), otherwise the <code>destroy</code> method is not called and the shutdown scripts are not triggered.

Now onto why I explicitly focused on where the scripts in <code>getShutdownScripts</code> and <code>getKeyspaceDrops</code> are executed. If, like I did in this post, you extended <code>AbstractCassandraConfiguration</code> and defined <code>getShutdownScripts</code> inside of it you could run into a problem because the scripts you defined will be executed twice. As mentioned a minute ago this is because <code>getShutdownScripts</code> is used twice within <code>AbstractCassandraConfiguration</code>, once when creating a session and once when creating a cluster (inherited from <code>AbstractClusterConfiguration</code>). Therefore if you try to drop a keyspace in the script, it will try drop it twice... leaving you with an error as the keyspace no longer exists. That is why in the above example I added the <code>IF EXISTS</code> statement to the CQL allowing the script to execute twice without problems. <code>getKeyspaceDrops</code> does not have this same issue as it is only set in <code>AbstractClusterConfiguration</code> and not used by the session created in <code>AbstractCassandraConfiguration</code>.

At the end of the day the CQL operations are more flexible, but if you are using <code>SchemaAction.CREATE_IF_NOT_EXITS</code> to create tables from entities then I do not think there is much else to do other than creating a keyspace at startup and dropping it (or truncating tables) at shutdown. I would suggest not using <code>getShutdownScripts</code> or <code>getKeyspaceDrops</code> in production as it is almost certainly going to cause problems if you are deleting your stored data, but for isolated testing they are very useful as you can debug through your code see it's outputs and then start again with a blank state.

The code used in this post can be found on my <a href="https://github.com/lankydan/spring-data-cassandra/tree/cassandra_startup_shutdown_scripts" target="_blank" rel="noopener">GitHub</a>.