document.addEventListener('DOMContentLoaded', function () {
    const calendarElement = document.getElementById('booking-calendar');
    const errorElement = document.getElementById('calendar-error');

    if (!calendarElement || typeof FullCalendar === 'undefined') {
        if (errorElement) {
            errorElement.hidden = false;
        }
        return;
    }

    const eventsUrl = calendarElement.dataset.eventsUrl || '/api/calendar/bookings';

    const calendar = new FullCalendar.Calendar(calendarElement, {
        initialView: 'dayGridMonth',
        locale: 'ru',
        firstDay: 1,
        height: 'auto',
        nowIndicator: true,
        displayEventEnd: true,
        headerToolbar: {
            left: 'prev,next today',
            center: 'title',
            right: 'dayGridMonth,timeGridWeek,listWeek'
        },
        buttonText: {
            today: 'Сегодня',
            month: 'Месяц',
            week: 'Неделя',
            list: 'Список'
        },
        eventTimeFormat: {
            hour: '2-digit',
            minute: '2-digit',
            hour12: false
        },
        events: {
            url: eventsUrl,
            method: 'GET',
            failure: function () {
                if (errorElement) {
                    errorElement.hidden = false;
                }
            }
        },
        loading: function (isLoading) {
            if (isLoading && errorElement) {
                errorElement.hidden = true;
            }
        },
        eventClick: function (info) {
            const event = info.event;
            const start = event.start ? event.start.toLocaleString('ru-RU') : '';
            const end = event.end ? event.end.toLocaleString('ru-RU') : '';
            const status = event.extendedProps.status === 'PENDING' ? 'ожидает решения' : 'подтверждено';

            alert(event.title + '\n' + start + ' - ' + end + '\nСтатус: ' + status);
        },
        eventDidMount: function (info) {
            const start = info.event.start ? info.event.start.toLocaleString('ru-RU') : '';
            const end = info.event.end ? info.event.end.toLocaleString('ru-RU') : '';
            info.el.title = info.event.title + '\n' + start + ' - ' + end;
        }
    });

    calendar.render();
});
