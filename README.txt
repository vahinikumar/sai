ONE BUTTON WHATSAPP CALLER

Purpose:
A very simple Android app with one large button. Tapping it opens the
specified WhatsApp chat. An AccessibilityService then looks for WhatsApp's
voice-call control and clicks it.

IMPORTANT:
1. This is a prototype. WhatsApp can change its UI at any time.
2. The AccessibilityService must be manually enabled in Android settings.
3. Change WHATSAPP_NUMBER in:
   app/src/main/java/com/example/onebuttonwhatsapp/MainActivity.java
4. For India, use 91 followed by the 10-digit number, with no + or spaces.
   Example: 919876543210
5. WhatsApp must already be installed and logged in.

BUILD:
This project uses Android Gradle Plugin 8.6.1 and compileSdk 35.
A cloud Android builder that accepts Gradle projects can build it.
Android Studio is not required on the user's PC.

NEXT:
We will test the project in a cloud builder, then fix the AccessibilityService
if WhatsApp's current button label does not match one of the labels used here.
