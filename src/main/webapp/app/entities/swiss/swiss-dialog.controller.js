(function() {
    'use strict';

    angular
        .module('tournamentControlApp')
        .controller('SwissDialogController', SwissDialogController);

    SwissDialogController.$inject = ['$timeout', '$scope', '$stateParams', '$uibModalInstance', 'entity', 'Swiss'];

    function SwissDialogController ($timeout, $scope, $stateParams, $uibModalInstance, entity, Swiss) {
        var vm = this;

        vm.swiss = entity;
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
            if (vm.swiss.id !== null) {
                Swiss.update(vm.swiss, onSaveSuccess, onSaveError);
            } else {
                Swiss.save(vm.swiss, onSaveSuccess, onSaveError);
            }
        }

        function onSaveSuccess (result) {
            $scope.$emit('tournamentControlApp:swissUpdate', result);
            $uibModalInstance.close(result);
            vm.isSaving = false;
        }

        function onSaveError () {
            vm.isSaving = false;
        }


    }
})();
