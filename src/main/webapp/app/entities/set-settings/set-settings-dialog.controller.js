(function() {
    'use strict';

    angular
        .module('tournamentControlApp')
        .controller('SetSettingsDialogController', SetSettingsDialogController);

    SetSettingsDialogController.$inject = ['$timeout', '$scope', '$stateParams', '$uibModalInstance', 'entity', 'SetSettings'];

    function SetSettingsDialogController ($timeout, $scope, $stateParams, $uibModalInstance, entity, SetSettings) {
        var vm = this;

        vm.setSettings = entity;
        vm.clear = clear;
        vm.save = save;

        $timeout(function (){
            angular.element('.form-group:eq(1)>input').focus();
        });

        function clear () {
            $uibModalInstance.dismiss('cancel');
        }

        function save () {
            vm.isSaving = true;
            if (vm.setSettings.id !== null) {
                SetSettings.update(vm.setSettings, onSaveSuccess, onSaveError);
            } else {
                SetSettings.save(vm.setSettings, onSaveSuccess, onSaveError);
            }
        }

        function onSaveSuccess (result) {
            $scope.$emit('tournamentControlApp:setSettingsUpdate', result);
            $uibModalInstance.close(result);
            vm.isSaving = false;
        }

        function onSaveError () {
            vm.isSaving = false;
        }


    }
})();
