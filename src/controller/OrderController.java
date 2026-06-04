/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Controller;

/**
 *
 * @author PHUONGTHAO
 */

import exception.InvalidQuantityException;
import exception.OutOfStockException;
import exception.PurchaseLimitExceededException;

import model.entity.Customer;
import model.entity.FlashSaleItem;

import model.enums.LockMechanism;

import repository.CustomerRepository;
import repository.FlashSaleItemRepository;
import repository.OrderRepository;

import java.io.IOException;

public class OrderController {

    private final FlashSaleItemRepository flashSaleItemRepository;

    private final CustomerRepository customerRepository;

    private final OrderRepository orderRepository;

    public OrderController() {

        this.flashSaleItemRepository =
                new FlashSaleItemRepository();

        this.customerRepository =
                new CustomerRepository();

        this.orderRepository =
                new OrderRepository();
    }

    /*
        Main order placement flow
     */
    public boolean placeOrder(String customerId,
                              String flashItemId,
                              int quantity,
                              LockMechanism mechanism)
            throws Exception {

        // =========================
        // VALIDATE QUANTITY
        // =========================

        if (quantity <= 0) {

            throw new InvalidQuantityException(
                    "Quantity must be greater than 0"
            );
        }

        // =========================
        // VALIDATE CUSTOMER
        // =========================

        Customer customer =
                customerRepository.findById(customerId);

        if (customer == null) {

            throw new IllegalArgumentException(
                    "Customer not found"
            );
        }

        // =========================
        // VALIDATE FLASH ITEM
        // =========================

        FlashSaleItem flashItem =
                flashSaleItemRepository.findById(flashItemId);

        if (flashItem == null) {

            throw new IllegalArgumentException(
                    "FlashSaleItem not found"
            );
        }

        // =========================
        // VALIDATE PURCHASE LIMIT
        // mỗi customer tối đa 2 item/event
        // =========================

        int purchasedQty =
                orderRepository.getPurchasedQuantity(
                        customerId,
                        flashItemId
                );

        if (purchasedQty + quantity > 2) {

            throw new PurchaseLimitExceededException(
                    "Maximum 2 items per customer/event"
            );
        }

        // =========================
        // VALIDATE STOCK
        // =========================

        int remainingStock =
                flashItem.getLimitedQty()
                - flashItem.getSoldQty();

        if (remainingStock < quantity) {

            throw new OutOfStockException(
                    "Not enough stock"
            );
        }

        // =========================
        // CHOOSE LOCK MECHANISM
        // =========================

        return switch (mechanism) {

            case NO_LOCK ->

                    flashSaleItemRepository
                            .sellWithNoLock(
                                    flashItemId,
                                    quantity
                            );

            case SYNCHRONIZED ->

                    flashSaleItemRepository
                            .sellWithSynchronized(
                                    flashItemId,
                                    quantity
                            );

            case FILE_LOCK ->

                    flashSaleItemRepository
                            .sellWithFileLock(
                                    flashItemId,
                                    quantity
                            );

            case OPTIMISTIC_LOCK ->

                    flashSaleItemRepository
                            .sellWithOptimisticLock(
                                    flashItemId,
                                    quantity
                            );
        };
    }
}

