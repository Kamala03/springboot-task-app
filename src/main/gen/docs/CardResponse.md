

# CardResponse


## Properties

| Name | Type | Description | Notes |
|------------ | ------------- | ------------- | -------------|
|**id** | **Integer** |  |  [optional] |
|**cardNumber** | **String** |  |  [optional] |
|**expirationDate** | **LocalDate** |  |  [optional] |
|**cardStatus** | [**CardStatusEnum**](#CardStatusEnum) |  |  [optional] |
|**balance** | **Double** |  |  [optional] |
|**user** | [**UserDtoResponse**](UserDtoResponse.md) |  |  [optional] |



## Enum: CardStatusEnum

| Name | Value |
|---- | -----|
| ACTIVE | &quot;ACTIVE&quot; |
| BLOCKED | &quot;BLOCKED&quot; |
| EXPIRED | &quot;EXPIRED&quot; |



