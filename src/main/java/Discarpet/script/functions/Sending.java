package Discarpet.script.functions;

import Discarpet.script.ScriptFuture;
import Discarpet.script.values.ChannelValue;
import Discarpet.script.values.EmbedBuilderValue;
import Discarpet.script.values.EmojiValue;
import Discarpet.script.values.MessageValue;
import carpet.script.Expression;
import carpet.script.LazyValue;
import carpet.script.argument.FunctionArgument;
import carpet.script.exception.InternalExpressionException;
import carpet.script.value.StringValue;
import carpet.script.value.Value;
import org.javacord.api.entity.message.Message;

import static carpet.script.LazyValue.TRUE;
import static carpet.script.LazyValue.FALSE;

import java.util.concurrent.CompletableFuture;

import static Discarpet.Discarpet.scarpetException;

public class Sending {
    public static void apply(Expression expr) {
        expr.addLazyFunction("dc_react", 2, (c, t, lv) -> {
            Value messageValue = lv.get(0).evalValue(c);
            Value emojiValue = lv.get(1).evalValue(c);

            if(!(messageValue instanceof MessageValue)) scarpetException("dc_react","message",0);
            if(!(emojiValue instanceof EmojiValue || emojiValue instanceof StringValue)) scarpetException("dc_react","emoji",1);

            if(!((MessageValue)messageValue).message.canYouAddNewReactions()) return FALSE;

            if(emojiValue instanceof EmojiValue) {
                ((MessageValue)messageValue).message.addReaction(((EmojiValue) emojiValue).emoji);
            } else {
                ((MessageValue)messageValue).message.addReaction(emojiValue.getString());
            }

            return TRUE;
        });


        expr.addLazyFunction("dc_send_message", -1, (c, t, lv) -> {
            if(!(lv.size()==2 || lv.size()==3)) throw new InternalExpressionException("'dc_send_message' requires two or tree arguments");

            Value channelValue = lv.get(0).evalValue(c);
            Value messageValue = lv.get(1).evalValue(c);

            if (!(channelValue instanceof ChannelValue)) scarpetException("dc_send_message","channel",0);

            CompletableFuture<Message> cf;
            if(messageValue instanceof EmbedBuilderValue) {
                cf = ((ChannelValue) channelValue).channel.sendMessage(((EmbedBuilderValue) messageValue).embedBuilder);
            } else {
                cf = ((ChannelValue) channelValue).channel.sendMessage(messageValue.getString());
            }

            if(lv.size()==3) {
                FunctionArgument<LazyValue> functionArgument = FunctionArgument.findIn(c, expr.module, lv, 2, false, false);
                ScriptFuture future = new ScriptFuture(c, functionArgument.function);
                cf.thenAccept(message -> {
                    future.execute(new MessageValue(message));
                });
            }

            return TRUE;
        });
    }
}
