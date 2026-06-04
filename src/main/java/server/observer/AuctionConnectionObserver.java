package server.observer;

import java.io.PrintWriter;

public class AuctionConnectionObserver
        implements AuctionObserver {

    private final PrintWriter writer;

    public AuctionConnectionObserver(
            PrintWriter writer
    ) {

        this.writer = writer;
    }

    @Override
    public void onAuctionEvent(
            String auctionId,
            String message
    ) {

        writer.println(message);
    }

    public PrintWriter getWriter() {
        return writer;
    }

    @Override
    public boolean equals(Object o) {

        if (this == o)
            return true;

        if (!(o instanceof AuctionConnectionObserver other))
            return false;

        return writer == other.writer;
    }

    @Override
    public int hashCode() {

        return System.identityHashCode(writer);
    }
}