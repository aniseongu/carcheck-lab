# CarCheck Lab Server README

This folder contains the AWS Lambda server code used by CarCheck Lab for login and profile storage.

## 1. AWS Services Used

- AWS Lambda
- Amazon DynamoDB
- API Gateway HTTP API or REST API

## 2. DynamoDB Table

Create a DynamoDB table:

```text
Table name: CarCheckUsers
Partition key: userId  (String)
```

The Lambda function stores these fields:

```text
userId       String
password     String  (course-demo only)
city         String
priority     String
hasProfile   Boolean
createdAt    Number
updatedAt    Number
```

## 3. Lambda Runtime

Recommended runtime:

```text
Python 3.12
```

Upload or paste `lambda_function.py` as the Lambda function code.

## 4. Lambda Environment Variable

Optional environment variable:

```text
TABLE_NAME=CarCheckUsers
```

If this variable is not set, the code uses `CarCheckUsers` by default.

## 5. IAM Permission

The Lambda execution role needs access to DynamoDB. Minimal actions:

```text
dynamodb:GetItem
dynamodb:PutItem
dynamodb:UpdateItem
```

For easier course-demo setup, `AmazonDynamoDBFullAccess` also works, but it is broader than necessary.

## 6. API Gateway

Create a POST route:

```text
POST /auth
```

Integrate it with the Lambda function.

The Android app sends JSON requests such as:

```json
{
  "action": "login",
  "userId": "test3",
  "password": "1234"
}
```

The Lambda function returns:

```json
{
  "success": true,
  "message": "Login successful.",
  "userId": "test3",
  "city": "Suwon",
  "priority": "Safety Nerd",
  "hasProfile": true
}
```

## 7. Android Endpoint

Set the API Gateway endpoint in:

```text
app/src/main/java/edu/skku/map/carchecklab/ServerAuthClient.kt
```

Example:

```kotlin
const val AUTH_API_URL: String = "https://YOUR_API_ID.execute-api.YOUR_REGION.amazonaws.com/auth"
```

## 8. Important Security Note

This server stores passwords as plain text only because this is a course demo project. This is not safe for production. For a real service, passwords must be hashed and salted, and authentication should use a secure method such as Cognito or a proper backend auth system.

