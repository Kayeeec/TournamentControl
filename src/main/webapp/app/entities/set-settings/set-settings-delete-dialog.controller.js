(function() {
    'use strict';

    angular
        .module('tournamentControlApp')
        .controller('SetSettingsDeleteController',SetSettingsDeleteController);

    SetSettingsDeleteController.$inject = ['$uibModalInstance', 'entity', 'SetSettings'];

    function SetSettingsDeleteController($uibModalInstance, entity, SetSettings) {
        var vm = this;

        vm.setSettings = entity;
        vm.clear = clear;
        vm.confirmDelete = confirmDelete;

        function clear () {
            $uibModalInstance.dismiss('cancel');
        }

        function confirmDelete (id) {
            SetSettings.delete({id: id},
                function () {
                    $uibModalInstance.close(true);
                });
        }
    }
})();
