export type EntitlementStatus="ACTIVE"|"EXPIRED"|"CANCELED"|"PENDING";
export type Entitlement={userId:string;productId:string;purchaseToken:string;status:EntitlementStatus;expiresAt?:string};
const store=new Map<string,Entitlement>();
export function upsertEntitlement(v:Entitlement){store.set(v.purchaseToken,v);return v}
export function getEntitlements(userId:string){return [...store.values()].filter(x=>x.userId===userId)}