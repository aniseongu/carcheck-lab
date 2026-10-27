import json
import os
import time
import base64
import boto3
from botocore.exceptions import ClientError

TABLE_NAME = os.environ.get("TABLE_NAME", "CarCheckUsers")
dynamodb = boto3.resource("dynamodb")
table = dynamodb.Table(TABLE_NAME)


def make_response(status_code, body):
    return {
        "statusCode": status_code,
        "headers": {
            "Content-Type": "application/json",
            "Access-Control-Allow-Origin": "*",
            "Access-Control-Allow-Headers": "Content-Type",
            "Access-Control-Allow-Methods": "OPTIONS,POST"
        },
        "body": json.dumps(body, ensure_ascii=False)
    }


def parse_body(event):
    raw_body = event.get("body") or "{}"

    if event.get("isBase64Encoded"):
        raw_body = base64.b64decode(raw_body).decode("utf-8")

    if isinstance(raw_body, dict):
        return raw_body

    return json.loads(raw_body)


def get_user(user_id):
    response = table.get_item(Key={"userId": user_id})
    return response.get("Item")


def make_user_result(success, message, item=None, user_id=""):
    if item is None:
        item = {}

    return {
        "success": success,
        "message": message,
        "userId": item.get("userId", user_id),
        "city": item.get("city", ""),
        "priority": item.get("priority", ""),
        "hasProfile": bool(item.get("hasProfile", False))
    }


def handle_register(data):
    user_id = str(data.get("userId", "")).strip()
    password = str(data.get("password", "")).strip()

    if not user_id or not password:
        return make_response(200, make_user_result(False, "User ID and password are required."))

    existing = get_user(user_id)

    if existing is not None:
        return make_response(200, make_user_result(False, "This user ID already exists.", user_id=user_id))

    now = int(time.time())
    item = {
        "userId": user_id,
        "password": password,
        "city": "",
        "priority": "",
        "hasProfile": False,
        "createdAt": now,
        "updatedAt": now
    }

    table.put_item(Item=item)

    return make_response(200, make_user_result(True, "Account created. Please set your profile.", item))


def handle_login(data):
    user_id = str(data.get("userId", "")).strip()
    password = str(data.get("password", "")).strip()

    if not user_id or not password:
        return make_response(200, make_user_result(False, "User ID and password are required."))

    item = get_user(user_id)

    if item is None:
        return make_response(200, make_user_result(False, "User ID was not found.", user_id=user_id))

    if str(item.get("password", "")) != password:
        return make_response(200, make_user_result(False, "Password is incorrect.", user_id=user_id))

    return make_response(200, make_user_result(True, "Login successful.", item))


def handle_update_profile(data):
    user_id = str(data.get("userId", "")).strip()
    city = str(data.get("city", "")).strip()
    priority = str(data.get("priority", "")).strip()

    if not user_id:
        return make_response(200, make_user_result(False, "User ID is required."))

    if not city or not priority:
        return make_response(200, make_user_result(False, "City and priority are required.", user_id=user_id))

    item = get_user(user_id)

    if item is None:
        return make_response(200, make_user_result(False, "User ID was not found.", user_id=user_id))

    table.update_item(
        Key={"userId": user_id},
        UpdateExpression="SET city = :city, priority = :priority, hasProfile = :hasProfile, updatedAt = :updatedAt",
        ExpressionAttributeValues={
            ":city": city,
            ":priority": priority,
            ":hasProfile": True,
            ":updatedAt": int(time.time())
        }
    )

    updated_item = get_user(user_id)
    return make_response(200, make_user_result(True, "Profile saved.", updated_item))


def handle_get_profile(data):
    user_id = str(data.get("userId", "")).strip()

    if not user_id:
        return make_response(200, make_user_result(False, "User ID is required."))

    item = get_user(user_id)

    if item is None:
        return make_response(200, make_user_result(False, "User ID was not found.", user_id=user_id))

    return make_response(200, make_user_result(True, "Profile loaded.", item))


def lambda_handler(event, context):
    try:
        method = ""

        request_context = event.get("requestContext") or {}
        http_info = request_context.get("http") or {}
        method = http_info.get("method") or event.get("httpMethod") or ""

        if method.upper() == "OPTIONS":
            return make_response(200, {"success": True, "message": "CORS preflight OK."})

        data = parse_body(event)
        action = str(data.get("action", "")).strip()

        if action == "register":
            return handle_register(data)

        if action == "login":
            return handle_login(data)

        if action == "updateProfile":
            return handle_update_profile(data)

        if action == "getProfile":
            return handle_get_profile(data)

        return make_response(200, make_user_result(False, "Unknown action."))

    except json.JSONDecodeError:
        return make_response(400, make_user_result(False, "Invalid JSON body."))

    except ClientError as error:
        return make_response(500, make_user_result(False, "DynamoDB error: " + str(error.response.get("Error", {}).get("Message", error))))

    except Exception as error:
        return make_response(500, make_user_result(False, "Server error: " + str(error)))
