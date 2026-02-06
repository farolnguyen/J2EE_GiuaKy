$(document).ready(function () {
    console.log('Cart JS loaded - checking for quantity inputs');
    console.log('Found quantity inputs:', $('.quantity-input').length);
    
    // Real-time quantity update with price recalculation
    $('.quantity-input').on('input change', function (e) {
        let $input = $(this);
        let quantity = parseInt($input.val());
        let bookId = $input.attr('data-book-id');
        let price = parseFloat($input.attr('data-price'));
        
        console.log('Event type:', e.type);
        console.log('Quantity changed:', quantity, 'Book ID:', bookId, 'Price:', price);
        
        if (quantity < 1 || isNaN(quantity)) {
            quantity = 1;
            $input.val(1);
        }
        
        // Update subtotal immediately
        let subtotal = price * quantity;
        let $subtotalCell = $('.subtotal-cell[data-book-id="' + bookId + '"]');
        let $subtotalValue = $subtotalCell.find('.subtotal-value');
        
        console.log('Subtotal cell found:', $subtotalCell.length);
        console.log('Updating subtotal to:', subtotal.toFixed(2));
        
        $subtotalValue.text(subtotal.toFixed(2));
        
        // Recalculate total
        updateCartTotals();
        
        // Send update to server (only on 'change' event, not 'input')
        if (e.type === 'change') {
            console.log('Sending AJAX request to update cart on server');
            $.ajax({
                url: '/cart/update/' + bookId + '/' + quantity,
                type: 'GET',
                success: function () {
                    console.log('Cart updated successfully on server');
                },
                error: function (xhr, status, error) {
                    console.error('Failed to update cart:', error);
                    alert('Failed to update cart. Reloading page...');
                    location.reload();
                }
            });
        }
    });
    
    function updateCartTotals() {
        let total = 0;
        $('.subtotal-value').each(function() {
            let text = $(this).text().replace(/,/g, '');
            let value = parseFloat(text);
            console.log('Subtotal value:', text, '-> parsed:', value);
            if (!isNaN(value)) {
                total += value;
            }
        });
        
        console.log('Total calculated:', total.toFixed(2));
        
        // Update summary subtotal
        $('.summary-subtotal span').text(total.toFixed(2));
        
        // Update main total
        $('.cart-total-amount span').text(total.toFixed(2));
        
        console.log('Totals updated in DOM');
    }
    
    // Initialize totals on page load
    updateCartTotals();
});