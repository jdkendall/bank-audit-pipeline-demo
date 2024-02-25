# Bank Audit Demo - Batch System
This system processes batch files every 5 minutes from a defined directory, submitting them to a processing queue.
We assume our upstream provider has already assigned a UUID to each of the transactions being shared, so we reuse 
theirs internally.

### Batch Format
The file does not have a header. Format is `{UUID}|{total}|{date}`. One transaction per line.

Total field may optionally be padded with spaces to right-align; padding to the left and right of the first non-whitespace character is discarded. (`.trim()`)

| Field | Description                                          |
|-------|------------------------------------------------------|
| uuid  | A UUIDv4 string uniquely identifying the transaction |
| total | The total amount spent on the transaction            |
| date  | ISO 8601 formatted timestamp                         |

### Example File

```
b53f22a3-40d1-4bb9-9678-f258b20b4193| 2912|2024-02-22T03:16:21Z
c6bed9d5-3cb9-44d5-b95b-07608437cdf3| 1020|2024-02-22T03:13:21Z
bf588782-7434-4fc8-bc97-e236b2bc8a68| 9459|2024-02-22T03:18:21Z
a36e8d3b-a995-4cf6-b481-bbed8236ea81|62100|2024-02-22T03:15:21Z
6c798543-5110-4843-a9f9-f7ac4c6ca813| 5023|2024-01-01T03:12:21Z
d49d97c0-a362-49d9-8092-f05be4270176| 8364|2024-02-19T03:11:21Z
b8d3ac89-fa19-4a88-ae68-c6a8b051698e| 4722|2023-12-10T03:19:21Z
88ba4a88-1d5e-4a7d-9a9d-1dbef2d260dd| 4360|2024-02-17T03:12:21Z
5b48060f-34cc-49f0-9e67-cc999343f6f5| 8555|2023-12-05T03:14:21Z
c5493542-e59b-4686-8f62-c5a4a3ea47a2| 4228|2024-02-22T03:17:21Z
```
