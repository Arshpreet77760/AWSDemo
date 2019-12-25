package model;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBAttribute;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBHashKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTable;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@DynamoDBTable(tableName = "PersonDetails")
public class PersonDetailsVo {
    @DynamoDBHashKey(attributeName = "Id")
    private int id;
    @DynamoDBAttribute(attributeName = "FirstName")
    private String firstName;
    @DynamoDBAttribute(attributeName = "LastName")
    private String lastName;
    @DynamoDBAttribute(attributeName = "Age")
    private int age;
    @DynamoDBAttribute(attributeName = "Address")
    private String address;
    @DynamoDBAttribute(attributeName = "Phone")
    private String phoneNo;
}