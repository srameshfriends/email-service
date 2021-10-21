function EmailLogger() {
    const self = this;
    self.setValidDate = function () {
        let dtToday = new Date(), month = dtToday.getMonth() + 1, day = dtToday.getDate(), year = dtToday.getFullYear();
        if(10 > month) {
            month = "0" + month;
        }
        if(10 > day) {
            day = "0" + day;
        }
        self.dateField.setAttribute('max', year + '-' + month + '-' + day);
        self.dateField.value = year + '-' + month + '-' + day;
    };
    self.loadLogs = function (date) {
        axios.get('/api/log/read?date=' + date)
            .then((response) => {
                const ele = document.getElementById("log-content");
                if(200 === response.status) {
                    ele.innerHTML = response.data;
                } else {
                    ele.innerHTML = response.statusText;
                }
            });
    }
    self.init = function () {
        self.dateField = document.getElementById("logging-date");
        self.actionRefresh = document.getElementById("actionRefresh");
        self.actionRefresh.addEventListener("click", function (evt){
            evt.preventDefault();
            const date = self.dateField.value;
            if(9 < date.toString().length) {
                self.loadLogs(date);
            }
        });
        self.setValidDate();
    }
}
function AdminConsole() {
    const self = this;
    self.reloadSettings = function () {
        axios.get('/api/admin/reload/settings')
            .then((response) => {
                const ele = document.getElementById("response-content");
                if(200 === response.status) {
                    ele.innerHTML = response.data;
                } else {
                    ele.innerHTML = response.statusText;
                }
            });
    }
    self.init = function () {
        self.settingsReloadBtn = document.getElementById("settingsReload");
        self.settingsReloadBtn.addEventListener("click", function (evt) {
            evt.preventDefault();
            self.reloadSettings();
        });
    }
}