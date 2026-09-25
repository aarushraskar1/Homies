import { google } from "googleapis";
const packageName=process.env.GOOGLE_PLAY_PACKAGE_NAME??"com.homiesgym.app";
export async function getSubscription(purchaseToken:string){
 const email=process.env.GOOGLE_SERVICE_ACCOUNT_EMAIL;
 const key=process.env.GOOGLE_SERVICE_ACCOUNT_PRIVATE_KEY?.replace(/\\n/g,"\n");
 if(!email||!key) throw new Error("Google Play credentials are not configured");
 const auth=new google.auth.JWT({email,key,scopes:["https://www.googleapis.com/auth/androidpublisher"]});
 return google.androidpublisher({version:"v3",auth}).purchases.subscriptionsv2.get({packageName,token:purchaseToken});
}