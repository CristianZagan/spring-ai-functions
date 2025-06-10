package guru.springframework.springaifunctions.services;


import guru.springframework.springaifunctions.functions.StockQuoteFunction;
import guru.springframework.springaifunctions.functions.WeatherServiceFunction;
import guru.springframework.springaifunctions.model.*;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.model.ModelOptionsUtils;
import org.springframework.ai.model.function.FunctionCallback;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Created by jt, Spring Framework Guru.
 */
@RequiredArgsConstructor
@Service
public class OpenAIServiceImpl implements OpenAIService {

    private String apiNinjasKey;

    final OpenAiChatModel openAiChatModel;

    //final ChatModel chatModel;

    public Answer getStockPrice(Question question) {
        var promptOptions = OpenAiChatOptions.builder()
                .functionCallbacks(List.of(FunctionCallback.builder()
                        .function("GetCurrentStockPrice", new StockQuoteFunction(apiNinjasKey))
                        .inputType(StockPriceRequest.class)
                        .description("Get the stock price for a ticker symbol")
                                .responseConverter(response -> {
                                    String schema = ModelOptionsUtils.getJsonSchema(StockPriceResponse.class, false);
                                    String json = ModelOptionsUtils.toJsonString(response);
                                    return schema + "\n" + json;
                                })
                        .build()))
                .build();

        Message userMessage = new PromptTemplate(question.question()).createMessage();
        Message systemMessage = new PromptTemplate("You are an agent which returns back a stock price for the given stock symbol (or ticker)").createMessage();

        var response = openAiChatModel.call(new Prompt(List.of(userMessage), promptOptions));

        return new Answer(response.getResult().getOutput().getContent());
    }

    @Override
    public Answer getAnswer(Question question) {
        var promptOptions = OpenAiChatOptions.builder()
                .functionCallbacks(List.of(FunctionCallback.builder()
                        .function("CurrentWeather", new WeatherServiceFunction(apiNinjasKey))
                        .inputType(WeatherRequest.class)
                        .description("Get the current weather for a location")
                                .responseConverter(response -> {
                                    String schema = ModelOptionsUtils.getJsonSchema(WeatherResponse.class, false);
                                    String json = ModelOptionsUtils.toJsonString(response);
                                    return schema + "\n" + json;
                                })
                        .build()))
                .build();

        Message userMessage = new PromptTemplate(question.question()).createMessage();

        var response = openAiChatModel.call(new Prompt(List.of(userMessage), promptOptions));

        return new Answer(response.getResult().getOutput().getContent());
    }
}
