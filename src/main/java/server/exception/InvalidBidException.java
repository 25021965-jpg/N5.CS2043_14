package server.exception;

public class InvalidBidException extends RuntimeException {
    // Ngoại lệ xảy ra khi số tiền đặt giá không hợp lệ
    public InvalidBidException(String message) {
        super(message);
    }
}