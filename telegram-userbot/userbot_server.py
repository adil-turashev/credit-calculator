import asyncio
import os
import re
import threading
from flask import Flask, request, jsonify
from dotenv import load_dotenv
from telethon import TelegramClient
from telethon.tl.functions.contacts import ImportContactsRequest, DeleteContactsRequest
from telethon.tl.types import InputPhoneContact

load_dotenv()

API_ID = int(os.getenv("API_ID"))
API_HASH = os.getenv("API_HASH")
PHONE_NUMBER = os.getenv("PHONE_NUMBER")
SESSION_NAME = os.getenv("SESSION_NAME", "credit2_session")
PORT = int(os.getenv("PORT", "5000"))

app = Flask(__name__)

loop = asyncio.new_event_loop()
client = TelegramClient(SESSION_NAME, API_ID, API_HASH, loop=loop)


def normalize_phone(phone: str) -> str:
    if phone is None:
        return ""

    digits = re.sub(r"\D", "", phone)

    if len(digits) == 11 and digits.startswith("8"):
        digits = "7" + digits[1:]

    if len(digits) == 10:
        digits = "7" + digits

    return "+" + digits


async def send_message_by_phone(phone: str, message: str):
    normalized_phone = normalize_phone(phone)

    contact = InputPhoneContact(
        client_id=0,
        phone=normalized_phone,
        first_name="Credit",
        last_name="Client"
    )

    result = await client(ImportContactsRequest([contact]))

    if not result.users:
        return {
            "ok": False,
            "error": "Пользователь с таким номером не найден в Telegram или скрыт настройками приватности",
            "phone": normalized_phone
        }

    user = result.users[0]

    await client.send_message(user.id, message)

    try:
        await client(DeleteContactsRequest(id=[user.id]))
    except Exception:
        pass

    return {
        "ok": True,
        "user_id": user.id,
        "phone": normalized_phone
    }


@app.route("/send", methods=["POST"])
def send():
    data = request.get_json(silent=True)

    if data is None:
        return jsonify({"ok": False, "error": "JSON body is required"}), 400

    phone = data.get("phone")
    message = data.get("message")

    if not phone or not message:
        return jsonify({"ok": False, "error": "phone and message are required"}), 400

    future = asyncio.run_coroutine_threadsafe(
        send_message_by_phone(phone, message),
        loop
    )

    try:
        result = future.result(timeout=30)
    except Exception as e:
        return jsonify({"ok": False, "error": str(e)}), 500

    if result.get("ok"):
        return jsonify(result), 200

    return jsonify(result), 400


@app.route("/health", methods=["GET"])
def health():
    return jsonify({"ok": True, "service": "telegram-userbot"})


async def start_client():
    await client.start(phone=PHONE_NUMBER)
    print("Userbot logged in successfully")


def start_loop():
    asyncio.set_event_loop(loop)
    loop.run_until_complete(start_client())
    loop.run_forever()


if __name__ == "__main__":
    thread = threading.Thread(target=start_loop, daemon=True)
    thread.start()

    app.run(host="127.0.0.1", port=PORT)