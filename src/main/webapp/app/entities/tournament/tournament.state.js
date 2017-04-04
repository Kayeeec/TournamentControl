(function() {
    'use strict';

    angular
        .module('tournamentControlApp')
        .config(stateConfig);

    stateConfig.$inject = ['$stateProvider'];

    function stateConfig($stateProvider) {
        $stateProvider
        .state('tournament', {
            parent: 'entity',
            url: '/tournament',
            data: {
                authorities: ['ROLE_USER'],
                pageTitle: 'tournamentControlApp.tournament.home.title'
            },
            views: {
                'content@': {
                    templateUrl: 'app/entities/tournament/tournaments.html',
                    controller: 'TournamentController',
                    controllerAs: 'vm'
                }
            },
            resolve: {
                translatePartialLoader: ['$translate', '$translatePartialLoader', function ($translate, $translatePartialLoader) {
                    $translatePartialLoader.addPart('tournament');
                    $translatePartialLoader.addPart('allVersusAll');
                    $translatePartialLoader.addPart('game');
                    $translatePartialLoader.addPart('global');
                    return $translate.refresh();
                }]
            }
        })
        .state('tournament-detail', {
            parent: 'tournament',
            url: '/tournament/{id}',
            data: {
                authorities: ['ROLE_USER'],
                pageTitle: 'tournamentControlApp.tournament.detail.title'
            },
            views: {
                'content@': {
                    templateUrl: 'app/entities/tournament/tournament-detail.html',
                    controller: 'TournamentDetailController',
                    controllerAs: 'vm'
                }
            },
            resolve: {
                translatePartialLoader: ['$translate', '$translatePartialLoader', function ($translate, $translatePartialLoader) {
                    $translatePartialLoader.addPart('tournament');
                    return $translate.refresh();
                }],
                entity: ['$stateParams', 'Tournament', function($stateParams, Tournament) {
                    return Tournament.get({id : $stateParams.id}).$promise;
                }],
                previousState: ["$state", function ($state) {
                    var currentStateData = {
                        name: $state.current.name || 'tournament',
                        params: $state.params,
                        url: $state.href($state.current.name, $state.params)
                    };
                    return currentStateData;
                }]
            }
        })
        .state('tournament-detail.edit', {
            parent: 'tournament-detail',
            url: '/detail/edit',
            data: {
                authorities: ['ROLE_USER']
            },
            onEnter: ['$stateParams', '$state', '$uibModal', function($stateParams, $state, $uibModal) {
                $uibModal.open({
                    templateUrl: 'app/entities/tournament/tournament-dialog.html',
                    controller: 'TournamentDialogController',
                    controllerAs: 'vm',
                    backdrop: 'static',
                    size: 'lg',
                    resolve: {
                        entity: ['Tournament', function(Tournament) {
                            return Tournament.get({id : $stateParams.id}).$promise;
                        }]
                    }
                }).result.then(function() {
                    $state.go('^', {}, { reload: false });
                }, function() {
                    $state.go('^');
                });
            }]
        })
        .state('tournament.new', {
            parent: 'tournament',
            url: '/new',
            data: {
                authorities: ['ROLE_USER']
            },
            onEnter: ['$stateParams', '$state', '$uibModal', function($stateParams, $state, $uibModal) {
                $uibModal.open({
                    templateUrl: 'app/entities/tournament/tournament-dialog.html',
                    controller: 'TournamentDialogController',
                    controllerAs: 'vm',
                    backdrop: 'static',
                    size: 'lg',
                    resolve: {
                        entity: function () {
                            return {
                                name: null,
                                note: null,
                                pointsForWinning: null,
                                pointsForLosing: null,
                                pointsForTie: null,
                                created: null,
                                id: null
                            };
                        }
                    }
                }).result.then(function() {
                    $state.go('tournament', null, { reload: 'tournament' });
                }, function() {
                    $state.go('tournament');
                });
            }]
        })
        .state('tournament.edit', {
            parent: 'tournament',
            url: '/{id}/edit',
            data: {
                authorities: ['ROLE_USER']
            },
            onEnter: ['$stateParams', '$state', '$uibModal', function($stateParams, $state, $uibModal) {
                $uibModal.open({
                    templateUrl: 'app/entities/tournament/tournament-dialog.html',
                    controller: 'TournamentDialogController',
                    controllerAs: 'vm',
                    backdrop: 'static',
                    size: 'lg',
                    resolve: {
                        entity: ['Tournament', function(Tournament) {
                            return Tournament.get({id : $stateParams.id}).$promise;
                        }]
                    }
                }).result.then(function() {
                    $state.go('tournament', null, { reload: 'tournament' });
                }, function() {
                    $state.go('^');
                });
            }]
        })
        .state('tournament.delete', {
            parent: 'tournament',
            url: '/{id}/delete',
            data: {
                authorities: ['ROLE_USER']
            },
            onEnter: ['$stateParams', '$state', '$uibModal', function($stateParams, $state, $uibModal) {
                $uibModal.open({
                    templateUrl: 'app/entities/tournament/tournament-delete-dialog.html',
                    controller: 'TournamentDeleteController',
                    controllerAs: 'vm',
                    size: 'md',
                    resolve: {
                        entity: ['Tournament', function(Tournament) {
                            return Tournament.get({id : $stateParams.id}).$promise;
                        }]
                    }
                }).result.then(function() {
                    $state.go('tournament', null, { reload: 'tournament' });
                }, function() {
                    $state.go('^');
                });
            }]
        });
    }

})();
