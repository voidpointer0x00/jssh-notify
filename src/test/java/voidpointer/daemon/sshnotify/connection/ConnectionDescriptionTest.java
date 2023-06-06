package voidpointer.daemon.sshnotify.connection;

import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class ConnectionDescriptionTest {

    @DataProvider
    public static Object[][] testParse() {
        return new Object[][] {
                {"""
                username: void
                userip: 127.0.0.1
                hostname: iceslime
                hostip: 45.93.200.95 fe80::c839:b8ff:fea9:845e
                
                Some custom message
                Goes right here
                Anything really.
                """,
                ConnectionDescription.builder()
                        .username("void")
                        .userIp("127.0.0.1")
                        .hostname("iceslime")
                        .hostIp("45.93.200.95 fe80::c839:b8ff:fea9:845e")
                        .customMessage("""
                                Some custom message
                                Goes right here
                                Anything really.""")
                        .build()},
                {"""
                username: void
                userip: 127.0.0.1
                hostname: iceslime
                hostip: 45.93.200.95 fe80::c839:b8ff:fea9:845e
                """,
                ConnectionDescription.builder()
                        .username("void")
                        .userIp("127.0.0.1")
                        .hostname("iceslime")
                        .hostIp("45.93.200.95 fe80::c839:b8ff:fea9:845e")
                        .build()}
        };
    }

    @Test(dataProvider="testParse")
    public void testParse(String input, ConnectionDescription expected) {
        Assert.assertEquals(ConnectionDescription.parse(input).orElse(null), expected);
    }
}