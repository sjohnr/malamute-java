package org.zeromq.malamute.protocol;

import static org.junit.Assert.*;

import org.junit.*;
import org.zeromq.api.*;
import org.zeromq.api.Message.Frame;
import org.zeromq.jzmq.*;

/**
 * Test MlmProtoSocket.
 */
public class MlmProtoSocketTest {
    private Context context;
    private Socket dealer;
    private Socket router;
    
    @Before
    public void setUp() {
        context = new ManagedContext();
        dealer = context.buildSocket(SocketType.DEALER)
            .bind("inproc://selftest");
        router = context.buildSocket(SocketType.ROUTER)
            .connect("inproc://selftest");
    }

    @Test
    public void testConnectionOpen() {
        MlmProtoSocket out = new MlmProtoSocket(dealer);
        MlmProtoSocket in = new MlmProtoSocket(router);
        
        ConnectionOpenMessage message = new ConnectionOpenMessage();
        message.setAddress("Life is short but Now lasts for ever");
        
        assertTrue(out.send(message));
        assertEquals(MlmProtoCodec.MessageType.CONNECTION_OPEN, in.receive());
        message = in.getConnectionOpen();
        assertEquals(message.getAddress(), "Life is short but Now lasts for ever");
        
        out.close();
        in.close();
    }

    @Test
    public void testConnectionPing() {
        MlmProtoSocket out = new MlmProtoSocket(dealer);
        MlmProtoSocket in = new MlmProtoSocket(router);
        
        ConnectionPingMessage message = new ConnectionPingMessage();
        
        assertTrue(out.send(message));
        assertEquals(MlmProtoCodec.MessageType.CONNECTION_PING, in.receive());
        message = in.getConnectionPing();
        
        out.close();
        in.close();
    }

    @Test
    public void testConnectionPong() {
        MlmProtoSocket out = new MlmProtoSocket(dealer);
        MlmProtoSocket in = new MlmProtoSocket(router);
        
        ConnectionPongMessage message = new ConnectionPongMessage();
        
        assertTrue(out.send(message));
        assertEquals(MlmProtoCodec.MessageType.CONNECTION_PONG, in.receive());
        message = in.getConnectionPong();
        
        out.close();
        in.close();
    }

    @Test
    public void testConnectionClose() {
        MlmProtoSocket out = new MlmProtoSocket(dealer);
        MlmProtoSocket in = new MlmProtoSocket(router);
        
        ConnectionCloseMessage message = new ConnectionCloseMessage();
        
        assertTrue(out.send(message));
        assertEquals(MlmProtoCodec.MessageType.CONNECTION_CLOSE, in.receive());
        message = in.getConnectionClose();
        
        out.close();
        in.close();
    }

    @Test
    public void testStreamWrite() {
        MlmProtoSocket out = new MlmProtoSocket(dealer);
        MlmProtoSocket in = new MlmProtoSocket(router);
        
        StreamWriteMessage message = new StreamWriteMessage();
        message.setStream("Life is short but Now lasts for ever");
        
        assertTrue(out.send(message));
        assertEquals(MlmProtoCodec.MessageType.STREAM_WRITE, in.receive());
        message = in.getStreamWrite();
        assertEquals(message.getStream(), "Life is short but Now lasts for ever");
        
        out.close();
        in.close();
    }

    @Test
    public void testStreamRead() {
        MlmProtoSocket out = new MlmProtoSocket(dealer);
        MlmProtoSocket in = new MlmProtoSocket(router);
        
        StreamReadMessage message = new StreamReadMessage();
        message.setStream("Life is short but Now lasts for ever");
        message.setPattern("Life is short but Now lasts for ever");
        
        assertTrue(out.send(message));
        assertEquals(MlmProtoCodec.MessageType.STREAM_READ, in.receive());
        message = in.getStreamRead();
        assertEquals(message.getStream(), "Life is short but Now lasts for ever");
        assertEquals(message.getPattern(), "Life is short but Now lasts for ever");
        
        out.close();
        in.close();
    }

    @Test
    public void testStreamSend() {
        MlmProtoSocket out = new MlmProtoSocket(dealer);
        MlmProtoSocket in = new MlmProtoSocket(router);
        
        StreamSendMessage message = new StreamSendMessage();
        message.setSubject("Life is short but Now lasts for ever");
        message.setContent(new Message("Hello, World"));
        
        assertTrue(out.send(message));
        assertEquals(MlmProtoCodec.MessageType.STREAM_SEND, in.receive());
        message = in.getStreamSend();
        assertEquals(message.getSubject(), "Life is short but Now lasts for ever");
        assertTrue(message.getContent().popString().equals("Hello, World"));
        
        out.close();
        in.close();
    }

    @Test
    public void testStreamDeliver() {
        MlmProtoSocket out = new MlmProtoSocket(dealer);
        MlmProtoSocket in = new MlmProtoSocket(router);
        
        StreamDeliverMessage message = new StreamDeliverMessage();
        message.setSender("Life is short but Now lasts for ever");
        message.setAddress("Life is short but Now lasts for ever");
        message.setSubject("Life is short but Now lasts for ever");
        message.setContent(new Message("Hello, World"));
        
        assertTrue(out.send(message));
        assertEquals(MlmProtoCodec.MessageType.STREAM_DELIVER, in.receive());
        message = in.getStreamDeliver();
        assertEquals(message.getSender(), "Life is short but Now lasts for ever");
        assertEquals(message.getAddress(), "Life is short but Now lasts for ever");
        assertEquals(message.getSubject(), "Life is short but Now lasts for ever");
        assertTrue(message.getContent().popString().equals("Hello, World"));
        
        out.close();
        in.close();
    }

    @Test
    public void testMailboxSend() {
        MlmProtoSocket out = new MlmProtoSocket(dealer);
        MlmProtoSocket in = new MlmProtoSocket(router);
        
        MailboxSendMessage message = new MailboxSendMessage();
        message.setAddress("Life is short but Now lasts for ever");
        message.setSubject("Life is short but Now lasts for ever");
        message.setTracker("Life is short but Now lasts for ever");
        message.setTimeout(123);
        message.setContent(new Message("Hello, World"));
        
        assertTrue(out.send(message));
        assertEquals(MlmProtoCodec.MessageType.MAILBOX_SEND, in.receive());
        message = in.getMailboxSend();
        assertEquals(message.getAddress(), "Life is short but Now lasts for ever");
        assertEquals(message.getSubject(), "Life is short but Now lasts for ever");
        assertEquals(message.getTracker(), "Life is short but Now lasts for ever");
        assertEquals(message.getTimeout(), Integer.valueOf(123));
        assertTrue(message.getContent().popString().equals("Hello, World"));
        
        out.close();
        in.close();
    }

    @Test
    public void testMailboxDeliver() {
        MlmProtoSocket out = new MlmProtoSocket(dealer);
        MlmProtoSocket in = new MlmProtoSocket(router);
        
        MailboxDeliverMessage message = new MailboxDeliverMessage();
        message.setSender("Life is short but Now lasts for ever");
        message.setAddress("Life is short but Now lasts for ever");
        message.setSubject("Life is short but Now lasts for ever");
        message.setTracker("Life is short but Now lasts for ever");
        message.setContent(new Message("Hello, World"));
        
        assertTrue(out.send(message));
        assertEquals(MlmProtoCodec.MessageType.MAILBOX_DELIVER, in.receive());
        message = in.getMailboxDeliver();
        assertEquals(message.getSender(), "Life is short but Now lasts for ever");
        assertEquals(message.getAddress(), "Life is short but Now lasts for ever");
        assertEquals(message.getSubject(), "Life is short but Now lasts for ever");
        assertEquals(message.getTracker(), "Life is short but Now lasts for ever");
        assertTrue(message.getContent().popString().equals("Hello, World"));
        
        out.close();
        in.close();
    }

    @Test
    public void testServiceSend() {
        MlmProtoSocket out = new MlmProtoSocket(dealer);
        MlmProtoSocket in = new MlmProtoSocket(router);
        
        ServiceSendMessage message = new ServiceSendMessage();
        message.setAddress("Life is short but Now lasts for ever");
        message.setSubject("Life is short but Now lasts for ever");
        message.setTracker("Life is short but Now lasts for ever");
        message.setTimeout(123);
        message.setContent(new Message("Hello, World"));
        
        assertTrue(out.send(message));
        assertEquals(MlmProtoCodec.MessageType.SERVICE_SEND, in.receive());
        message = in.getServiceSend();
        assertEquals(message.getAddress(), "Life is short but Now lasts for ever");
        assertEquals(message.getSubject(), "Life is short but Now lasts for ever");
        assertEquals(message.getTracker(), "Life is short but Now lasts for ever");
        assertEquals(message.getTimeout(), Integer.valueOf(123));
        assertTrue(message.getContent().popString().equals("Hello, World"));
        
        out.close();
        in.close();
    }

    @Test
    public void testServiceOffer() {
        MlmProtoSocket out = new MlmProtoSocket(dealer);
        MlmProtoSocket in = new MlmProtoSocket(router);
        
        ServiceOfferMessage message = new ServiceOfferMessage();
        message.setAddress("Life is short but Now lasts for ever");
        message.setPattern("Life is short but Now lasts for ever");
        
        assertTrue(out.send(message));
        assertEquals(MlmProtoCodec.MessageType.SERVICE_OFFER, in.receive());
        message = in.getServiceOffer();
        assertEquals(message.getAddress(), "Life is short but Now lasts for ever");
        assertEquals(message.getPattern(), "Life is short but Now lasts for ever");
        
        out.close();
        in.close();
    }

    @Test
    public void testServiceDeliver() {
        MlmProtoSocket out = new MlmProtoSocket(dealer);
        MlmProtoSocket in = new MlmProtoSocket(router);
        
        ServiceDeliverMessage message = new ServiceDeliverMessage();
        message.setSender("Life is short but Now lasts for ever");
        message.setAddress("Life is short but Now lasts for ever");
        message.setSubject("Life is short but Now lasts for ever");
        message.setTracker("Life is short but Now lasts for ever");
        message.setContent(new Message("Hello, World"));
        
        assertTrue(out.send(message));
        assertEquals(MlmProtoCodec.MessageType.SERVICE_DELIVER, in.receive());
        message = in.getServiceDeliver();
        assertEquals(message.getSender(), "Life is short but Now lasts for ever");
        assertEquals(message.getAddress(), "Life is short but Now lasts for ever");
        assertEquals(message.getSubject(), "Life is short but Now lasts for ever");
        assertEquals(message.getTracker(), "Life is short but Now lasts for ever");
        assertTrue(message.getContent().popString().equals("Hello, World"));
        
        out.close();
        in.close();
    }

    @Test
    public void testOk() {
        MlmProtoSocket out = new MlmProtoSocket(dealer);
        MlmProtoSocket in = new MlmProtoSocket(router);
        
        OkMessage message = new OkMessage();
        message.setStatusCode(123);
        message.setStatusReason("Life is short but Now lasts for ever");
        
        assertTrue(out.send(message));
        assertEquals(MlmProtoCodec.MessageType.OK, in.receive());
        message = in.getOk();
        assertEquals(message.getStatusCode(), Integer.valueOf(123));
        assertEquals(message.getStatusReason(), "Life is short but Now lasts for ever");
        
        out.close();
        in.close();
    }

    @Test
    public void testError() {
        MlmProtoSocket out = new MlmProtoSocket(dealer);
        MlmProtoSocket in = new MlmProtoSocket(router);
        
        ErrorMessage message = new ErrorMessage();
        message.setStatusCode(123);
        message.setStatusReason("Life is short but Now lasts for ever");
        
        assertTrue(out.send(message));
        assertEquals(MlmProtoCodec.MessageType.ERROR, in.receive());
        message = in.getError();
        assertEquals(message.getStatusCode(), Integer.valueOf(123));
        assertEquals(message.getStatusReason(), "Life is short but Now lasts for ever");
        
        out.close();
        in.close();
    }

    @Test
    public void testCredit() {
        MlmProtoSocket out = new MlmProtoSocket(dealer);
        MlmProtoSocket in = new MlmProtoSocket(router);
        
        CreditMessage message = new CreditMessage();
        message.setAmount(123);
        
        assertTrue(out.send(message));
        assertEquals(MlmProtoCodec.MessageType.CREDIT, in.receive());
        message = in.getCredit();
        assertEquals(message.getAmount(), Integer.valueOf(123));
        
        out.close();
        in.close();
    }

    @Test
    public void testConfirm() {
        MlmProtoSocket out = new MlmProtoSocket(dealer);
        MlmProtoSocket in = new MlmProtoSocket(router);
        
        ConfirmMessage message = new ConfirmMessage();
        message.setTracker("Life is short but Now lasts for ever");
        message.setStatusCode(123);
        message.setStatusReason("Life is short but Now lasts for ever");
        
        assertTrue(out.send(message));
        assertEquals(MlmProtoCodec.MessageType.CONFIRM, in.receive());
        message = in.getConfirm();
        assertEquals(message.getTracker(), "Life is short but Now lasts for ever");
        assertEquals(message.getStatusCode(), Integer.valueOf(123));
        assertEquals(message.getStatusReason(), "Life is short but Now lasts for ever");
        
        out.close();
        in.close();
    }

    @Test
    public void testStreamCancel() {
        MlmProtoSocket out = new MlmProtoSocket(dealer);
        MlmProtoSocket in = new MlmProtoSocket(router);
        
        StreamCancelMessage message = new StreamCancelMessage();
        message.setStream("Life is short but Now lasts for ever");
        
        assertTrue(out.send(message));
        assertEquals(MlmProtoCodec.MessageType.STREAM_CANCEL, in.receive());
        message = in.getStreamCancel();
        assertEquals(message.getStream(), "Life is short but Now lasts for ever");
        
        out.close();
        in.close();
    }
}