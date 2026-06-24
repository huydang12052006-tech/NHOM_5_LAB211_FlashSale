package controller;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import model.Entity.CartItem;
import repository.CartItemRepository;

public class CartController {

    private final CartItemRepository cartRepository;

    public CartController() {
        this(new CartItemRepository());
    }

    public CartController(CartItemRepository cartRepository) {
        this.cartRepository = cartRepository;
    }

    public List<CartItem> getCartByCustomer(String customerId) {
        List<CartItem> result = new ArrayList<CartItem>();
        for (CartItem item : cartRepository.findAll()) {
            if (customerId.equalsIgnoreCase(item.getCustomerId())) {
                result.add(item);
            }
        }
        return result;
    }

    public void addItem(String customerId, String flashItemId, String productId, int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be greater than 0");
        }
        for (CartItem item : getCartByCustomer(customerId)) {
            if (same(item.getFlashItemId(), flashItemId) && same(item.getProductId(), productId)) {
                item.setQuantity(item.getQuantity() + quantity);
                item.setUpdatedAt(LocalDateTime.now());
                cartRepository.update(item);
                return;
            }
        }
        LocalDateTime now = LocalDateTime.now();
        cartRepository.save(new CartItem(cartRepository.generateNextId(), now, now,
                customerId, flashItemId, productId, quantity));
    }

    public boolean removeItem(String customerId, String cartItemId) {
        CartItem item = cartRepository.findById(cartItemId);
        if (item == null || !customerId.equalsIgnoreCase(item.getCustomerId())) {
            return false;
        }
        try {
            return cartRepository.delete(cartItemId);
        } catch (IOException e) {
            return false;
        }
    }

    public void clearCart(String customerId) {
        for (CartItem item : getCartByCustomer(customerId)) {
            removeItem(customerId, item.getId());
        }
    }

    private boolean same(String first, String second) {
        return first == null ? second == null : first.equals(second);
    }
}
