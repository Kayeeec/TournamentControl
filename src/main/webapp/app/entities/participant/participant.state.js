(function() {
    'use strict';

    angular
        .module('tournamentControlApp')
        .config(stateConfig);

    stateConfig.$inject = ['$stateProvider'];

    function stateConfig($stateProvider) {
        $stateProvider
        .state('participant', {
            parent: 'entity',
            url: '/participant',
            data: {
                authorities: ['ROLE_USER'],
                pageTitle: 'tournamentControlApp.participant.home.title'
            },
            views: {
                'content@': {
                    templateUrl: 'app/entities/participant/participants.html',
                    controller: 'ParticipantController',
                    controllerAs: 'vm'
                }
            },
            resolve: {
                translatePartialLoader: ['$translate', '$translatePartialLoader', function ($translate, $translatePartialLoader) {
                    $translatePartialLoader.addPart('participant');
                    $translatePartialLoader.addPart('global');
                    return $translate.refresh();
                }]
            }
        })
        .state('participant-detail', {
            parent: 'participant',
            url: '/participant/{id}',
            data: {
                authorities: ['ROLE_USER'],
                pageTitle: 'tournamentControlApp.participant.detail.title'
            },
            views: {
                'content@': {
                    templateUrl: 'app/entities/participant/participant-detail.html',
                    controller: 'ParticipantDetailController',
                    controllerAs: 'vm'
                }
            },
            resolve: {
                translatePartialLoader: ['$translate', '$translatePartialLoader', function ($translate, $translatePartialLoader) {
                    $translatePartialLoader.addPart('participant');
                    return $translate.refresh();
                }],
                entity: ['$stateParams', 'Participant', function($stateParams, Participant) {
                    return Participant.get({id : $stateParams.id}).$promise;
                }],
                previousState: ["$state", function ($state) {
                    var currentStateData = {
                        name: $state.current.name || 'participant',
                        params: $state.params,
                        url: $state.href($state.current.name, $state.params)
                    };
                    return currentStateData;
                }]
            }
        })
        .state('participant-detail.edit', {
            parent: 'participant-detail',
            url: '/detail/edit',
            data: {
                authorities: ['ROLE_USER']
            },
            onEnter: ['$stateParams', '$state', '$uibModal', function($stateParams, $state, $uibModal) {
                $uibModal.open({
                    templateUrl: 'app/entities/participant/participant-dialog.html',
                    controller: 'ParticipantDialogController',
                    controllerAs: 'vm',
                    backdrop: 'static',
                    size: 'lg',
                    resolve: {
                        entity: ['Participant', function(Participant) {
                            return Participant.get({id : $stateParams.id}).$promise;
                        }]
                    }
                }).result.then(function() {
                    $state.go('^', {}, { reload: false });
                }, function() {
                    $state.go('^');
                });
            }]
        })
        .state('participant.new', {
            parent: 'participant',
            url: '/new',
            data: {
                authorities: ['ROLE_USER']
            },
            onEnter: ['$stateParams', '$state', '$uibModal', function($stateParams, $state, $uibModal) {
                $uibModal.open({
                    templateUrl: 'app/entities/participant/participant-dialog.html',
                    controller: 'ParticipantDialogController',
                    controllerAs: 'vm',
                    backdrop: 'static',
                    size: 'lg',
                    resolve: {
                        entity: function () {
                            return {
                                id: null
                            };
                        }
                    }
                }).result.then(function() {
                    $state.go('participant', null, { reload: 'participant' });
                }, function() {
                    $state.go('participant');
                });
            }]
        })
        .state('participant.edit', {
            parent: 'participant',
            url: '/{id}/edit',
            data: {
                authorities: ['ROLE_USER']
            },
            onEnter: ['$stateParams', '$state', '$uibModal', function($stateParams, $state, $uibModal) {
                $uibModal.open({
                    templateUrl: 'app/entities/participant/participant-dialog.html',
                    controller: 'ParticipantDialogController',
                    controllerAs: 'vm',
                    backdrop: 'static',
                    size: 'lg',
                    resolve: {
                        entity: ['Participant', function(Participant) {
                            return Participant.get({id : $stateParams.id}).$promise;
                        }]
                    }
                }).result.then(function() {
                    $state.go('participant', null, { reload: 'participant' });
                }, function() {
                    $state.go('^');
                });
            }]
        })
        .state('participant.delete', {
            parent: 'participant',
            url: '/{id}/delete',
            data: {
                authorities: ['ROLE_USER']
            },
            onEnter: ['$stateParams', '$state', '$uibModal', function($stateParams, $state, $uibModal) {
                $uibModal.open({
                    templateUrl: 'app/entities/participant/participant-delete-dialog.html',
                    controller: 'ParticipantDeleteController',
                    controllerAs: 'vm',
                    size: 'md',
                    resolve: {
                        entity: ['Participant', function(Participant) {
                            return Participant.get({id : $stateParams.id}).$promise;
                        }]
                    }
                }).result.then(function() {
                    $state.go('participant', null, { reload: 'participant' });
                }, function() {
                    $state.go('^');
                });
            }]
        });
    }

})();
