document.addEventListener('DOMContentLoaded', function() {
    const forms = document.querySelectorAll('form');
    forms.forEach(form => {
        form.addEventListener('submit', function(e) {
            const password = form.querySelector('input[name="password"]');
            if (password && password.value.length < 6 && password.required) {
                alert('Пароль должен содержать минимум 6 символов');
                e.preventDefault();
                return;
            }

            const price = form.querySelector('input[name="price"]');
            if (price && price.value < 0) {
                alert('Цена не может быть отрицательной');
                e.preventDefault();
                return;
            }

            const quantity = form.querySelector('input[name="quantity"]');
            if (quantity && quantity.value < 0) {
                alert('Количество не может быть отрицательным');
                e.preventDefault();
            }
        });
    });

    const orderForm = document.querySelector('form[action="/orders"]');
    if (orderForm) {
        orderForm.addEventListener('submit', function(e) {
            const productId = this.querySelector('select[name="productId"]').value;
            const quantity = this.querySelector('input[name="quantity"]').value;
            if (!productId) {
                alert('Выберите товар');
                e.preventDefault();
                return;
            }
            if (!quantity || quantity < 1) {
                alert('Укажите количество больше 0');
                e.preventDefault();
                return;
            }
        });
    }

    const cancelOrderForms = document.querySelectorAll('.cancel-order-form');
    cancelOrderForms.forEach(form => {
        form.addEventListener('submit', function(e) {
            const orderId = this.getAttribute('data-order-id');
            if (!confirm(`Вы уверены, что хотите отменить заказ #${orderId}?`)) {
                e.preventDefault();
            }
        });
    });

    const sortableHeaders = document.querySelectorAll('th.sortable');

    function parseRuDate(str) {
        if (!str) return -Infinity;
        if (!str.match(/^\d{2}\.\d{2}\.\d{4}/)) return null;

        const parts = str.trim().split(' ');
        const dateParts = parts[0].split('.');
        const timeParts = parts[1] ? parts[1].split(':') : ['00', '00'];

        return new Date(
            parseInt(dateParts[2]),
            parseInt(dateParts[1]) - 1,
            parseInt(dateParts[0]),
            parseInt(timeParts[0]),
            parseInt(timeParts[1])
        ).getTime();
    }

    sortableHeaders.forEach(th => {
        th.addEventListener('click', () => {
            const table = th.closest('table');
            const tbody = table.querySelector('tbody');
            const rows = Array.from(tbody.querySelectorAll('tr'));
            const index = Array.from(th.parentNode.children).indexOf(th);
            const indicator = th.querySelector('.sort-indicator');
            const isAsc = indicator.classList.contains('asc');
            const direction = !isAsc ? 1 : -1;

            table.querySelectorAll('.sort-indicator').forEach(ind => {
                if (ind !== indicator) ind.classList.remove('asc', 'desc');
            });

            indicator.classList.toggle('asc', !isAsc);
            indicator.classList.toggle('desc', isAsc);

            rows.sort((rowA, rowB) => {
                const cellA = rowA.children[index].innerText.trim();
                const cellB = rowB.children[index].innerText.trim();
                const dateA = parseRuDate(cellA);
                const dateB = parseRuDate(cellB);

                if (dateA !== null && dateB !== null) {
                    return (dateA - dateB) * direction;
                }

                const rawNumA = cellA.replace(/[^\d.-]/g, '');
                const rawNumB = cellB.replace(/[^\d.-]/g, '');

                const isNumericCol = rawNumA.length > 0 && !isNaN(parseFloat(rawNumA)) &&
                    rawNumB.length > 0 && !isNaN(parseFloat(rawNumB));

                if (isNumericCol) {
                    const hasLetters = /[a-zA-Zа-яА-Я]/.test(cellA.replace('руб.', ''));
                    if (!hasLetters) {
                        return (parseFloat(rawNumA) - parseFloat(rawNumB)) * direction;
                    }
                }

                return cellA.localeCompare(cellB, undefined, { numeric: true, sensitivity: 'base' }) * direction;
            });

            tbody.append(...rows);
        });
    });

    function getDeclension(number, words) {
        const cases = [2, 0, 1, 1, 1, 2];
        return words[(number % 100 > 4 && number % 100 < 20) ? 2 : cases[Math.min(number % 10, 5)]];
    }

    function updateTableHeaders() {
        const tableSections = document.querySelectorAll('.table-section');

        tableSections.forEach(section => {
            const h2 = section.querySelector('h2');
            const table = section.querySelector('table');

            if (!h2 || !table) return;

            if (!h2.dataset.originalText) {
                h2.dataset.originalText = h2.textContent;
            }

            const originalText = h2.dataset.originalText;
            const tbody = table.querySelector('tbody');
            const rowCount = tbody ? tbody.querySelectorAll('tr').length : 0;

            let words = [];
            if (originalText.includes('категорий')) {
                words = ['категория', 'категории', 'категорий'];
            } else if (originalText.includes('товаров')) {
                words = ['товар', 'товара', 'товаров'];
            } else if (originalText.includes('пользователей')) {
                words = ['пользователь', 'пользователя', 'пользователей'];
            } else if (originalText.includes('заказов')) {
                words = ['заказ', 'заказа', 'заказов'];
            }

            if (words.length > 0) {
                const declension = getDeclension(rowCount, words);
                h2.textContent = `${originalText} (всего ${rowCount} ${declension} в таблице)`;
            }
        });
    }

    updateTableHeaders();

    function calculateProductStats() {
        const avgPriceEl = document.getElementById('avg-price');
        const avgQtyEl = document.getElementById('avg-quantity');
        const totalItemsEl = document.getElementById('total-items');

        if (!avgPriceEl || !avgQtyEl) return;

        const table = document.querySelector('.users-table');
        if (!table) return;

        const tbody = table.querySelector('tbody');
        const rows = tbody.querySelectorAll('tr');
        let totalPrice = 0;
        let totalQty = 0;
        let count = 0;

        rows.forEach(row => {
            const priceCell = row.children[4];
            const qtyCell = row.children[5];
            if (priceCell && qtyCell) {
                const priceText = priceCell.innerText.replace(/[^\d.,-]/g, '').replace(',', '.');
                const qtyText = qtyCell.innerText.replace(/[^\d.,-]/g, '').replace(',', '.');
                const price = parseFloat(priceText);
                const qty = parseFloat(qtyText);
                if (!isNaN(price) && !isNaN(qty)) {
                    totalPrice += price;
                    totalQty += qty;
                    count++;
                }
            }
        });

        if (count > 0) {
            avgPriceEl.innerText = (totalPrice / count).toFixed(2); // Округляем до 2 знаков
            avgQtyEl.innerText = (totalQty / count).toFixed(1);     // Округляем до 1 знака
            if (totalItemsEl) totalItemsEl.innerText = count;
        } else {
            avgPriceEl.innerText = "0.00";
            avgQtyEl.innerText = "0";
            if (totalItemsEl) totalItemsEl.innerText = "0";
        }
    }

    function calculateOrderStats() {
        const avgOrderSumEl = document.getElementById('avg-order-sum');
        const avgOrderQtyEl = document.getElementById('avg-order-qty');
        const avgDeliveryTimeEl = document.getElementById('avg-delivery-time');
        const totalOrdersEl = document.getElementById('total-orders');

        if (!avgOrderSumEl || !avgOrderQtyEl || !avgDeliveryTimeEl) return;

        const table = document.querySelector('.users-table');
        if (!table) return;

        const tbody = table.querySelector('tbody');
        const rows = tbody.querySelectorAll('tr');

        let totalSum = 0;
        let totalQty = 0;
        let totalDeliveryDays = 0;
        let deliveredCount = 0;
        let totalCount = rows.length;

        rows.forEach(row => {
            const qtyCell = row.children[3];
            const totalPriceCell = row.children[5];
            const statusCell = row.children[6];
            const createdAtCell = row.children[7];
            const updatedAtCell = row.children[8];

            if (qtyCell && totalPriceCell && statusCell) {
                const totalPriceText = totalPriceCell.innerText.replace(/[^\d.,-]/g, '').replace(',', '.');
                const totalPrice = parseFloat(totalPriceText);
                if (!isNaN(totalPrice)) {
                    totalSum += totalPrice;
                }

                const qtyText = qtyCell.innerText.replace(/[^\d.,-]/g, '').replace(',', '.');
                const qty = parseFloat(qtyText);
                if (!isNaN(qty)) {
                    totalQty += qty;
                }

                const statusText = statusCell.innerText.trim();
                if (statusText === 'DELIVERED' && createdAtCell && updatedAtCell) {
                    const createdText = createdAtCell.innerText.trim();
                    const updatedText = updatedAtCell.innerText.trim();
                    if (createdText && updatedText && updatedText !== '-') {
                        const createdTime = parseRuDate(createdText);
                        const updatedTime = parseRuDate(updatedText);
                        if (createdTime && updatedTime && createdTime > 0 && updatedTime > 0) {
                            const diffDays = (updatedTime - createdTime) / (1000 * 60 * 60 * 24);
                            totalDeliveryDays += diffDays;
                            deliveredCount++;
                        }
                    }
                }
            }
        });

        if (totalCount > 0) {
            avgOrderSumEl.innerText = (totalSum / totalCount).toFixed(2);
            avgOrderQtyEl.innerText = (totalQty / totalCount).toFixed(1);
            if (totalOrdersEl) totalOrdersEl.innerText = totalCount;
        } else {
            avgOrderSumEl.innerText = "0.00";
            avgOrderQtyEl.innerText = "0";
            if (totalOrdersEl) totalOrdersEl.innerText = "0";
        }

        if (deliveredCount > 0) {
            const avgDays = totalDeliveryDays / deliveredCount;
            if (avgDays >= 1) {
                avgDeliveryTimeEl.innerText = avgDays.toFixed(1) + ' дн.';
            } else {
                const avgHours = avgDays * 24;
                avgDeliveryTimeEl.innerText = avgHours.toFixed(1) + ' ч.';
            }
        } else {
            avgDeliveryTimeEl.innerText = '-';
        }
    }

    window.calculateTotal = function() {
        const quantityInput = document.getElementById('quantity');
        const totalInput = document.getElementById('totalPrice');
        if (!quantityInput || !totalInput) return;
        const productPrice = parseFloat(quantityInput.dataset.price) || 0;
        const quantity = parseInt(quantityInput.value) || 0;
        if (quantity && productPrice) {
            const total = productPrice * quantity;
            totalInput.value = total.toFixed(2) + ' ₽';
        } else {
            totalInput.value = '';
        }
    };

    calculateProductStats();
    calculateOrderStats();
});