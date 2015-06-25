package com.example.cassandra;



import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.Host;
import com.datastax.driver.core.Metadata;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Session;

public class SimpleClient {
   private Cluster cluster;
   private Session session;

   public void connect(String node) {
      cluster = Cluster.builder()
            .addContactPoint(node).build();
      Metadata metadata = cluster.getMetadata();
      System.out.printf("Connected to cluster: %s\n", 
            metadata.getClusterName());
      for ( Host host : metadata.getAllHosts() ) {
         System.out.printf("Datatacenter: %s; Host: %s; Rack: %s\n",
               host.getDatacenter(), host.getAddress(), host.getRack());
      }
   }
   
   public void createSchema() { 
	   session = cluster.connect();
	   session.execute("DROP KEYSPACE IF EXISTS simplex6");
	   session.execute("CREATE KEYSPACE simplex6 WITH replication " + 
			      "= {'class':'SimpleStrategy', 'replication_factor':3};");
	   session.execute(
			      "CREATE TABLE simplex6.songs (" +
			            "id uuid PRIMARY KEY," + 
			            "title text," + 
			            "album text," + 
			            "artist text," + 
			            "tags set<text>," + 
			            "data blob" + 
			            ");");
			session.execute(
			      "CREATE TABLE simplex6.playlists (" +
			            "id uuid," +
			            "title text," +
			            "album text, " + 
			            "artist text," +
			            "song_id uuid," +
			            "PRIMARY KEY (id, title, album, artist)" +
			            ");");
   }
   public void loadData() { 
	   session.execute(
			      "INSERT INTO simplex6.songs (id, title, album, artist, tags) " +
			      "VALUES (" +
			          "7ad54392-bcdd-35a6-8417-4e047860b377," +
			          "'La Petite Tonkinoise'," +
			          "'Bye Bye Blackbird'," +
			          "'Joséphine Baker'," +
			          "{'jazz', '2013'})" +
			          ";");
			session.execute(
			      "INSERT INTO simplex6.playlists (id, song_id, title, album, artist) " +
			      "VALUES (" +
			          "7ad54392-bcdd-35a6-8417-4e047860b377," +
			          "296e9c04-9bec-3085-827d-c17d3df2122a," +
			          "'La Petite Tonkinoise'," +
			          "'Bye Bye Blackbird'," +
			          "'Joséphine Baker'" +
			          ");");
   }
   public void querySchema() {
	   ResultSet results = session.execute("SELECT * FROM simplex6.playlists " +
		        "WHERE id = 296e9c04-9bec-3085-827d-c17d3df2122a;");
	   System.out.println(String.format("%-30s\t%-20s\t%-20s\n%s", "title", "album", "artist",
		       "-------------------------------+-----------------------+--------------------"));
		for (Row row : results) {
		    System.out.println(String.format("%-30s\t%-20s\t%-20s", row.getString("title"),
		    row.getString("album"),  row.getString("artist")));
		}
		System.out.println();
		}
  // public void close() {
    //  cluster.shutdown();
   //}

   public static void main(String[] args) {
      SimpleClient client = new SimpleClient();
      client.connect("127.0.0.1");
     // client.close();
      client.createSchema();
      client.loadData();
      client.querySchema();
   }
}