package io.github.chumper.webserver.data;

import com.mongodb.MongoClient;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoDatabase;

import org.bson.Document;

import java.time.Instant;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * The {@link MongoDbCommentRepository} stores comments from topics inside a mongoDB document.
 * Each document stores a topicId and an array of comments.
 */
public class MongoDbCommentRepository implements CommentRepository {

  /**
   * The client to interact with the mongoDb
   */
  private final MongoClient client;

  /**
   * The database to store the comments in
   */
  private final MongoDatabase database;

  public MongoDbCommentRepository(String mongoHost, int mongoPort) {
    this.client = new MongoClient(mongoHost, mongoPort);
    this.database = client.getDatabase("topics");
  }


  @Override
  public Stream<Comment> all() {
    FindIterable<Document> documents = database.getCollection("comments").find();
    return StreamSupport.stream(documents.spliterator(), false)
        .map(this::from);
  }

  @Override
  public void add(Comment comment) {
    database.getCollection("comments").insertOne(from(comment));
  }

  @Override
  public void clear() {
    database.getCollection("comments").deleteMany(new Document());
  }

  /**
   * Method to convert a document to a comment
   * @param document the document to convert
   * @return the converted comment
   */
  private Comment from(Document document) {
    return new Comment(
        Instant.parse(document.getString("posted")),
        document.getString("name"),
        document.getString("message")
    );
  }

  /**
   * Method to convert a comment to a document
   * @param comment the comment to convert
   * @return the converted document
   */
  private Document from(Comment comment) {
    Document document = new Document();
    document.put("posted", comment.posted.toString());
    document.put("name", comment.name);
    document.put("message", comment.message);
    return document;
  }
}
