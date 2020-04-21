package biz.cits.reactive.message;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;

public class MsgGenerator {

    //    private static String[] clients = new String[]{"ABCDE", "ABCDF", "ABCDG", "ABCDH", "ABCDI", "ABCEF", "ABCEG", "ABCEH", "ABCEI", "ABCFG", "ABCFH", "ABCFI", "ABCGH", "ABCGI", "ABCHI", "ABDEF", "ABDEG", "ABDEH", "ABDEI", "ABDFG", "ABDFH", "ABDFI", "ABDGH", "ABDGI", "ABDHI", "ABEFG", "ABEFH", "ABEFI", "ABEGH", "ABEGI", "ABEHI", "ABFGH", "ABFGI", "ABFHI", "ABGHI", "ACDEF", "ACDEG", "ACDEH", "ACDEI", "ACDFG", "ACDFH", "ACDFI", "ACDGH", "ACDGI", "ACDHI", "ACEFG", "ACEFH", "ACEFI", "ACEGH", "ACEGI", "ACEHI", "ACFGH", "ACFGI", "ACFHI", "ACGHI", "ADEFG", "ADEFH", "ADEFI", "ADEGH", "ADEGI", "ADEHI", "ADFGH", "ADFGI", "ADFHI", "ADGHI", "AEFGH", "AEFGI", "AEFHI", "AEGHI", "AFGHI", "BCDEF", "BCDEG", "BCDEH", "BCDEI", "BCDFG", "BCDFH", "BCDFI", "BCDGH", "BCDGI", "BCDHI", "BCEFG", "BCEFH", "BCEFI", "BCEGH", "BCEGI", "BCEHI", "BCFGH", "BCFGI", "BCFHI", "BCGHI", "BDEFG", "BDEFH", "BDEFI", "BDEGH", "BDEGI", "BDEHI", "BDFGH", "BDFGI", "BDFHI", "BDGHI", "BEFGH", "BEFGI", "BEFHI", "BEGHI", "BFGHI", "CDEFG", "CDEFH", "CDEFI", "CDEGH", "CDEGI", "CDEHI", "CDFGH", "CDFGI", "CDFHI", "CDGHI", "CEFGH", "CEFGI", "CEFHI", "CEGHI", "CFGHI", "DEFGH", "DEFGI", "DEFHI", "DEGHI", "DFGHI", "EFGHI"};
    private static String[] clients = new String[]{"ABCDE", "ABCDF", "ABCDG"};

    private static Map<String, Integer> clientMessageIds = Collections.synchronizedMap(new HashMap<>());
    static AtomicInteger ctr = new AtomicInteger(0);

    private static String getClient() {
        if (ctr.get() == clients.length - 1) {
            ctr.set(0);
        } else {
            ctr.set(ctr.addAndGet(1));
        }
        return clients[ctr.get()];
    }

    public static ArrayList<Map.Entry<String, String>> getMessages(int numMessage) {
        ArrayList<Map.Entry<String, String>> messages = new ArrayList<>();
        IntStream.range(0, numMessage).forEach(i -> {
            String client = getClient();
            messages.add(new AbstractMap.SimpleEntry<>(client, getClientMessage(client)));
        });
        return messages;
    }

    private static String getClientMessage(String client) {
        clientMessageIds.putIfAbsent(client, 1);
        clientMessageIds.compute(client, (k, v) -> (v == null) ? 1 : v + 1);
        return client + "," + clientMessageIds.get(client);
    }
}
