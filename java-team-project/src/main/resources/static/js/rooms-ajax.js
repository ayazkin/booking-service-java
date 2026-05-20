$(function () {
    const $button = $('#ajax-room-preview');
    const $panel = $('#ajax-room-results');
    const $form = $('.filters');

    if (!$button.length || !$panel.length || !$form.length) {
        return;
    }

    $button.on('click', function () {
        const requestData = $form.serializeArray().filter(function (field) {
            return field.name !== 'filter' && field.name !== 'size';
        });

        if (!$form.find('input[name="activeOnly"]').is(':checked')) {
            requestData.push({ name: 'activeOnly', value: 'false' });
        }

        requestData.push({ name: 'size', value: '5' });

        $button.prop('disabled', true).text('Проверяем...');
        $panel.removeClass('ajax-panel-error').prop('hidden', false).html('<p>Ищем подходящие аудитории...</p>');

        $.ajax({
            url: $button.data('url'),
            method: 'GET',
            data: $.param(requestData),
            dataType: 'json',
            headers: {
                'X-Requested-With': 'XMLHttpRequest'
            }
        }).done(function (rooms) {
            renderRooms(Array.isArray(rooms) ? rooms : []);
        }).fail(function (xhr) {
            const message = xhr.responseJSON && xhr.responseJSON.message
                ? xhr.responseJSON.message
                : 'Не удалось выполнить AJAX-поиск';
            $panel.addClass('ajax-panel-error').html('<p>' + escapeHtml(message) + '</p>');
        }).always(function () {
            $button.prop('disabled', false).text('Быстрый предпросмотр');
        });
    });

    function renderRooms(rooms) {
        if (rooms.length === 0) {
            $panel.html('<p>По этим фильтрам аудитории не найдены.</p>');
            return;
        }

        const items = rooms.map(function (room) {
            const equipment = room.equipment && room.equipment.length
                ? room.equipment.map(function (item) { return escapeHtml(item.name); }).join(', ')
                : '-';

            return '<li>' +
                '<strong>' + escapeHtml(room.number) + ' - ' + escapeHtml(room.name) + '</strong>' +
                '<span>' + room.capacity + ' мест, ' + room.floor + ' этаж</span>' +
                '<small>' + equipment + '</small>' +
                '</li>';
        }).join('');

        $panel.html(
            '<div class="ajax-panel-header">' +
            '<strong>AJAX-предпросмотр</strong>' +
            '<span>Показано до 5 аудиторий без перезагрузки страницы</span>' +
            '</div>' +
            '<ul class="ajax-room-list">' + items + '</ul>'
        );
    }

    function escapeHtml(value) {
        return String(value == null ? '' : value)
            .replace(/&/g, '&amp;')
            .replace(/</g, '&lt;')
            .replace(/>/g, '&gt;')
            .replace(/"/g, '&quot;')
            .replace(/'/g, '&#039;');
    }
});
