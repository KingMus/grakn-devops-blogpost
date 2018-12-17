package grakn;

import ai.grakn.GraknTxType;
import ai.grakn.Keyspace;
import ai.grakn.client.Grakn;
import ai.grakn.concept.Attribute;
import ai.grakn.concept.AttributeType;
import ai.grakn.concept.Entity;
import ai.grakn.concept.EntityType;
import ai.grakn.concept.Relationship;
import ai.grakn.concept.RelationshipType;
import ai.grakn.concept.Role;
import ai.grakn.util.SimpleURI;

public class GraphCreator {

	public static void main(String[] args) {
		/**
		 * Set up graph for Graln.AI on server
		 */

		Grakn grakn = new Grakn(new SimpleURI("localhost:48555"));
		Grakn.Session session = grakn.session(Keyspace.of("grakndevsss"));
		Grakn.Transaction tx = session.transaction(GraknTxType.WRITE);

		AttributeType identifier = tx.putAttributeType("identifier", AttributeType.DataType.STRING);
		AttributeType firstname = tx.putAttributeType("firstname", AttributeType.DataType.STRING);
		AttributeType surname = tx.putAttributeType("surname", AttributeType.DataType.STRING);
		AttributeType middlename = tx.putAttributeType("middlename", AttributeType.DataType.STRING);
		AttributeType picture = tx.putAttributeType("picture", AttributeType.DataType.STRING);
		AttributeType age = tx.putAttributeType("age", AttributeType.DataType.LONG);
		AttributeType birthDate = tx.putAttributeType("birth-date", AttributeType.DataType.DATE);
		AttributeType deathDate = tx.putAttributeType("death-date", AttributeType.DataType.DATE);
		AttributeType gender = tx.putAttributeType("gender", AttributeType.DataType.STRING);

		Role spouse1 = tx.putRole("spouse1");
		Role spouse2 = tx.putRole("spouse2");
		RelationshipType marriage = tx.putRelationshipType("marriage").relates(spouse1).relates(spouse2);
		marriage.has(picture);

		Role parent = tx.putRole("parent");
		Role child = tx.putRole("child");
		RelationshipType parentship = tx.putRelationshipType("parentship").relates(parent).relates(child);

		EntityType person = tx.putEntityType("person").plays(parent).plays(child).plays(spouse1).plays(spouse2);

		person.has(identifier);
		person.has(firstname);
		person.has(surname);
		person.has(middlename);
		person.has(picture);
		person.has(age);
		person.has(birthDate);
		person.has(deathDate);
		person.has(gender);

		// Load data

		Attribute johnName = firstname.create("John"); // Create the attribute
		person.create().has(johnName); // Link it to an entity

		// Create the attributes
		johnName = firstname.create("John");
		Attribute maryName = firstname.create("Mary");

		// Create the entities
		Entity john = person.create();
		Entity mary = person.create();

		// Create the actual relationships
		Relationship theMarriage = marriage.create().assign(spouse1, john).assign(spouse2, mary);

		tx.commit();
		System.err.println("Done");
	}

}
