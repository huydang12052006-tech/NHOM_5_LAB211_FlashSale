/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package controller;

/**
 *
 * @author PHUONGTHAO
 */

import exception.InvalidQuantityException;
import exception.OutOfStockException;
import exception.PurchaseLimitExceededException;

import model.Entity.Customer;
import model.Entity.FlashSaleItem;

import model.Enum.LockMechanism;

import repository.CustomerRepository;
import repository.FlashSaleItemRepository;
import repository.OrderRepository;


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

        boolean result;

        switch (mechanism) {
            case NO_LOCK:
                result = flashSaleItemRepository
                        .sellWithNoLock(
                                flashItemId,
                                quantity
                        );
                break;

            case SYNCHRONIZED:
                result = flashSaleItemRepository
                        .sellWithSynchronized(
                                flashItemId,
                                quantity
                        );
                break;

            case FILE_LOCK:
                result = flashSaleItemRepository
                        .sellWithFileLock(
                                flashItemId,
                                quantity
                        );
                break;

            case OPTIMISTIC_LOCK:
                result = flashSaleItemRepository
                        .sellWithOptimisticLock(
                                flashItemId,
                                quantity
                        );
                break;

            default:
                throw new IllegalArgumentException("Unknown lock mechanism");
        }

        return result;
    }
}

