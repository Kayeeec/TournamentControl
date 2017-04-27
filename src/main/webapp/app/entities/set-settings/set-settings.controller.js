(function() {
    'use strict';

    angular
        .module('tournamentControlApp')
        .controller('SetSettingsController', SetSettingsController);

    SetSettingsController.$inject = ['SetSettings'];

    function SetSettingsController(SetSettings) {

        var vm = this;

        vm.setSettings = [];

        loadAll();

        function loadAll() {
            SetSettings.query(function(result) {
                vm.setSettings = result;
                vm.searchQuery = null;
            });
        }
    }
})();
