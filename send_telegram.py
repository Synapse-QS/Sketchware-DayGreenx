import os
import sys

def main():
    try:
        from pyrogram import Client

        api_id = os.getenv("TELEGRAM_API_ID")
        api_hash = os.getenv("TELEGRAM_API_HASH")
        bot_token = os.getenv("TELEGRAM_BOT_TOKEN")
        chat_id = os.getenv("TELEGRAM_CHAT_ID")

        # لو أي بيانات ناقصة، اخرج من غير خطأ
        if not all([api_id, api_hash, bot_token, chat_id]):
            print("⚠️ بيانات تليجرام ناقصة، تم تخطي الإرسال.")
            return

        app = Client(
            "my_bot",
            api_id=int(api_id),
            api_hash=api_hash,
            bot_token=bot_token,
        )

        with app:
            app.send_message(chat_id, "✅ تم بناء التطبيق بنجاح!")
            print("✅ تم إرسال الرسالة على تليجرام.")

    except Exception as e:
        # أي خطأ هنا ميوقفش البناء
        print(f"⚠️ فشل إرسال تليجرام: {e}")
        print("⏭️ تم تخطي الإرسال ومتابعة البناء.")

if __name__ == "__main__":
    main()
    sys.exit(0)   # دايماً يرجع نجاح
