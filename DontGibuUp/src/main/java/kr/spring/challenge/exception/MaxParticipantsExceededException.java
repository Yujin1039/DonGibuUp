package kr.spring.challenge.exception;

public class MaxParticipantsExceededException extends RuntimeException{
	public MaxParticipantsExceededException(String message) {
		super(message);
	}
}
