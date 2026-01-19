package org.example.taskproject.mapper;



import org.example.taskproject.dto.CardResponse;
import org.example.taskproject.dto.CardRequest;
import org.example.taskproject.entity.CardEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring",uses = {UserMapper.class},unmappedTargetPolicy = ReportingPolicy.IGNORE)
public abstract class CardMapper {
    public abstract CardEntity toCardEntity(CardRequest cardRequest);

    @Mapping(source = "cardNumber",target = "cardNumber" ,qualifiedByName = "CardNumberMasking")
    public abstract CardResponse toCardDtoResponse(CardEntity cardEntity);


    @Named("CardNumberMasking")
    public String cardNumberMasking(String cardNumber){
        String maskedCardNumber = "";
        for(int i=0;i<cardNumber.length()-4;i +=5) {
            maskedCardNumber += cardNumber.substring(i,i+4).replaceAll("\\d", "*") + " ";
        }
        return maskedCardNumber + cardNumber.substring(cardNumber.length()-4);
    }
}
