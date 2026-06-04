package model;

public class AuctionUserState {

    private final String auctionId;

    private ParticipationStatus participationStatus =
            ParticipationStatus.NOT_JOINED;

    private PaymentStatus paymentStatus =
            PaymentStatus.UNPAID;

    public AuctionUserState(String auctionId) {
        this.auctionId = auctionId;
    }

    public String getAuctionId() {
        return auctionId;
    }

    // ==================== PARTICIPATION ====================
    public ParticipationStatus getParticipationStatus() {
        return participationStatus;
    }

    public void setParticipationStatus(ParticipationStatus participationStatus) {
        this.participationStatus = participationStatus;
    }

    public boolean isJoined() {
        return participationStatus == ParticipationStatus.JOINED;
    }

    // ==================== PAYMENT ====================
    public PaymentStatus getPaymentStatus() {
        return paymentStatus;
    }

    public void setPaymentStatus(PaymentStatus paymentStatus) {
        this.paymentStatus = paymentStatus;
    }
}