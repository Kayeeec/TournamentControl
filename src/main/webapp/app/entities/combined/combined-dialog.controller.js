(function() {
    'use strict';

    angular
        .module('tournamentControlApp')
        .controller('CombinedDialogController', CombinedDialogController);

    CombinedDialogController.$inject = ['$timeout', '$scope', '$stateParams', '$uibModalInstance', '$q', 'entity', 'Combined', 'Participant', 'Tournament'];

    function CombinedDialogController ($timeout, $scope, $stateParams, $uibModalInstance, $q, entity, Combined, Participant, Tournament) {
        var vm = this;

        vm.combined = entity;
        vm.clear = clear;
        vm.datePickerOpenStatus = {};
        vm.openCalendar = openCalendar;
        vm.save = save;
        vm.participants = Participant.query();
        vm.playoffs = Tournament.query({filter: 'combined-is-null'});
        $q.all([vm.combined.$promise, vm.playoffs.$promise]).then(function() {
            if (!vm.combined.playoff || !vm.combined.playoff.id) {
                return $q.reject();
            }
            return Tournament.get({id : vm.combined.playoff.id}).$promise;
        }).then(function(playoff) {
            vm.playoffs.push(playoff);
        });
        vm.tournaments = Tournament.query();

        $timeout(function (){
            angular.element('.form-group:eq(1)>input').focus();
        });

        function clear () {
            $uibModalInstance.dismiss('cancel');
        }

        function save () {
            vm.isSaving = true;
            if (vm.combined.id !== null) {
                Combined.update(vm.combined, onSaveSuccess, onSaveError);
            } else {
                Combined.save(vm.combined, onSaveSuccess, onSaveError);
            }
        }

        function onSaveSuccess (result) {
            $scope.$emit('tournamentControlApp:combinedUpdate', result);
            $uibModalInstance.close(result);
            vm.isSaving = false;
        }

        function onSaveError () {
            vm.isSaving = false;
        }

        vm.datePickerOpenStatus.created = false;

        function openCalendar (date) {
            vm.datePickerOpenStatus[date] = true;
        }
    }
})();
