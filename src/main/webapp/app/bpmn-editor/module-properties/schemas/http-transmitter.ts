import { AGREEMENT_MODE, ModuleSchema } from '../schema';

/**
 * `HttpTransmitter` is the largest module at 34 fields. Order follows the `renderComponents.push`
 * list in the Vue panel's `HttpTransmitter:HttpTransmitter` case. Four of those entries were
 * commented out there — `retryCountNumber`, `authUserName`, `authPassword`, `authType` — but their
 * components and getters/setters exist and the properties are real, so they are kept here at the
 * positions the comments held, the same way `httpReceiverSchema` keeps fields its panel case
 * omitted.
 */
export const httpTransmitterSchema: ModuleSchema = {
  type: 'HttpTransmitter:HttpTransmitter',
  label: 'HttpTransmitter',
  fields: [
    { name: 'agreementMode', label: 'Agreement Mode', kind: 'select', options: AGREEMENT_MODE },
    { name: 'partyUrl', label: 'Party Url', kind: 'text' },
    { name: 'partyUrlJslt', label: 'Party Url JSLT', kind: 'textarea' },
    { name: 'httpMethod', label: 'Http Method', kind: 'select', options: ['GET', 'POST', 'DELETE', 'PUT'] },
    // Capitalised in the Vue getter — `${prefix}:ContentType`, alone among this module's
    // properties. Lower-casing it here would read and write an attribute no diagram has.
    { name: 'ContentType', label: 'Content Type', kind: 'text' },
    { name: 'partyTimeOut', label: 'Party Time Out (ms)', kind: 'number' },
    { name: 'httpHeaders', label: 'Http Headers', kind: 'textarea' },
    { name: 'httpBody', label: 'Http Body', kind: 'textarea' },
    { name: 'queryParams', label: 'Query Params', kind: 'textarea' },
    { name: 'authUrl', label: 'Auth Url', kind: 'text' },
    { name: 'authHeaders', label: 'Auth Headers', kind: 'textarea' },
    { name: 'authBody', label: 'Auth Body', kind: 'textarea' },
    { name: 'authKey', label: 'Auth Key', kind: 'textarea' },
    { name: 'authResponseDetector', label: 'Auth Response Detector', kind: 'textarea' },
    { name: 'successResponseCodes', label: 'Success Response Codes', kind: 'text' },
    { name: 'successResponsePattern', label: 'Success Response Pattern', kind: 'text' },
    { name: 'retryResponseCodes', label: 'Retry Response Codes', kind: 'text' },
    { name: 'retryResponsePattern', label: 'Retry Response Pattern', kind: 'text' },
    { name: 'retryCountNumber', label: 'Retry Count Number', kind: 'number' },
    { name: 'responseMergePolicy', label: 'Response Merge Policy', kind: 'select', options: ['REQ', 'RES', 'BOTH'] },
    { name: 'authUserName', label: 'Auth Username', kind: 'text' },
    { name: 'authPassword', label: 'Auth Password', kind: 'text' },
    { name: 'authType', label: 'Auth Type', kind: 'text' },
    // Stored as the strings '0' and '1'; the Vue select showed them as No and Yes.
    {
      name: 'isCacheAble',
      label: 'Is Cache Able',
      kind: 'select',
      options: [
        { value: '0', label: 'No' },
        { value: '1', label: 'Yes' },
      ],
    },
    { name: 'cacheExpiryTime', label: 'Cache Expiry Time (ms)', kind: 'number' },
    { name: 'cacheKey', label: 'Cache Key', kind: 'textarea' },
    {
      name: 'backoffType',
      label: 'Back Off Type',
      kind: 'select',
      options: [
        { value: 'FIXED', label: 'Fixed' },
        { value: 'EXPONENTIAL', label: 'Exponential' },
      ],
    },
    { name: 'fixedDelay', label: 'Fixed Delay (ms)', kind: 'number' },
    { name: 'baseDelay', label: 'Base Delay (ms)', kind: 'number' },
    { name: 'multiplier', label: 'Multiplier (ms)', kind: 'number' },
    { name: 'maxDelay', label: 'Max Delay (ms)', kind: 'number' },
    { name: 'maxAttempts', label: 'Max Attempts', kind: 'number' },
    { name: 'overAllTimeOut', label: 'OverAll Time Out (ms)', kind: 'number' },
    { name: 'commentDesc', label: 'Comment Desc', kind: 'text' },
  ],
};
