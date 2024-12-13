package com.jpmc.midascore;

import com.jpmc.midascore.entity.Incentive;
import com.jpmc.midascore.entity.UserRecord;
import com.jpmc.midascore.foundation.Transaction;
import com.jpmc.midascore.repository.TransactionRecordRepository;
import com.jpmc.midascore.repository.UserRepository;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import javax.swing.text.html.Option;
import java.util.Optional;

@Component public class KafkaListeners {

    private final UserRepository userRepository;
    private final TransactionRecordRepository transactionRecordRepository;

    public KafkaListeners(UserRepository userRepository, TransactionRecordRepository transactionRecordRepository) {
        this.userRepository = userRepository;
        this.transactionRecordRepository = transactionRecordRepository;
    }

    @KafkaListener(id = "transactionId1", topics = "transaction-topic")
    public void listener(Transaction transaction) {
        System.out.println("Listener received: " + transaction);
    }

    @KafkaListener(id = "transactionId2", topics = "transaction-topic")
    public void processTransaction(Transaction transaction) {
        Optional<UserRecord> optionalSender = Optional.ofNullable(userRepository.findById(transaction.getSenderId()));
        UserRecord sender = optionalSender.orElse(null);
        Optional<UserRecord> optionalRecipient = Optional.ofNullable(userRepository.findById(transaction.getRecipientId()));
        UserRecord recipient = optionalRecipient .orElse(null);

        // validate transaction
        if (sender != null && recipient != null && sender.getBalance() >= transaction.getAmount()) {
            Incentive incentive = IncentiveService.getIncentive(transaction);

            sender.setBalance(sender.getBalance() - transaction.getAmount());
            recipient.setBalance(recipient.getBalance() + transaction.getAmount() + Incentive.getAmount());

            TransactionRecord transactionRecord = new TransactionRecord(sender, recipient, transaction.getAmount());
            transactionRecord.setIncentive(Incentive.getAmount());

            System.out.printf("Transaction stored: \n\tSender: " + sender.getName() + "\n\tRecipient: " + recipient.getName() + "\n\tAmount: " + "%.2f%n", transaction.getAmount());
            userRepository.save(sender);
            userRepository.save(recipient);
            transactionRecordRepository.save(transactionRecord);
        }


    }

}
