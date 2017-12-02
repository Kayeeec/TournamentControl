(function() {
    'use strict';

    angular
        .module('tournamentControlApp')
        .config(stateConfig);

    stateConfig.$inject = ['$stateProvider'];

    function stateConfig($stateProvider) {
        $stateProvider
        .state('combined', {
            parent: 'entity',
            url: '/combined?page&sort&search',
            data: {
                authorities: ['ROLE_USER'],
                pageTitle: 'tournamentControlApp.combined.home.title'
            },
            views: {
                'content@': {
                    templateUrl: 'app/entities/combined/combineds.html',
                    controller: 'CombinedController',
                    controllerAs: 'vm'
                }
            },
            params: {
                page: {
                    value: '1',
                    squash: true
                },
                sort: {
                    value: 'id,asc',
                    squash: true
                },
                search: null
            },
            resolve: {
                pagingParams: ['$stateParams', 'PaginationUtil', function ($stateParams, PaginationUtil) {
                    return {
                        page: PaginationUtil.parsePage($stateParams.page),
                        sort: $stateParams.sort,
                        predicate: PaginationUtil.parsePredicate($stateParams.sort),
                        ascending: PaginationUtil.parseAscending($stateParams.sort),
                        search: $stateParams.search
                    };
                }],
                translatePartialLoader: ['$translate', '$translatePartialLoader', function ($translate, $translatePartialLoader) {
                    $translatePartialLoader.addPart('combined');
                    $translatePartialLoader.addPart('tournamentType');
                    $translatePartialLoader.addPart('global');
                    $translatePartialLoader.addPart('swiss');
                    $translatePartialLoader.addPart('allVersusAll');
                    $translatePartialLoader.addPart('tournament');
                    $translatePartialLoader.addPart('setSettings');
                    $translatePartialLoader.addPart('game');
                    $translatePartialLoader.addPart('gameSet');
                    return $translate.refresh();
                }]
            }
        })
        .state('combined-detail', {
            parent: 'combined',
            url: '/combined/{id}',
            data: {
                authorities: ['ROLE_USER'],
                pageTitle: 'tournamentControlApp.combined.detail.title'
            },
            views: {
                'content@': {
                    templateUrl: 'app/entities/combined/combined-detail.html',
                    controller: 'CombinedDetailController',
                    controllerAs: 'vm'
                }
            },
            resolve: {
                translatePartialLoader: ['$translate', '$translatePartialLoader', function ($translate, $translatePartialLoader) {
                    $translatePartialLoader.addPart('combined');
                    $translatePartialLoader.addPart('tournamentType');
                    $translatePartialLoader.addPart('tournamentType');
                    return $translate.refresh();
                }],
                entity: ['$stateParams', 'Combined', function($stateParams, Combined) {
                    return Combined.get({id : $stateParams.id}).$promise;
                }],
                previousState: ["$state", function ($state) {
                    var currentStateData = {
                        name: $state.current.name || 'combined',
                        params: $state.params,
                        url: $state.href($state.current.name, $state.params)
                    };
                    return currentStateData;
                }]
            }
        })
        .state('combined-detail.edit', {
            parent: 'combined-detail',
            url: '/detail/edit',
            data: {
                authorities: ['ROLE_USER']
            },
            onEnter: ['$stateParams', '$state', '$uibModal', function($stateParams, $state, $uibModal) {
                $uibModal.open({
                    templateUrl: 'app/entities/combined/combined-dialog.html',
                    controller: 'CombinedDialogController',
                    controllerAs: 'vm',
                    backdrop: 'static',
                    size: 'lg',
                    resolve: {
                        entity: ['Combined', function(Combined) {
                            return Combined.get({id : $stateParams.id}).$promise;
                        }]
                    }
                }).result.then(function() {
                    $state.go('^', {}, { reload: false });
                }, function() {
                    $state.go('^');
                });
            }]
        })
        .state('combined.new', {
            parent: 'combined',
            url: '/new',
            data: {
                authorities: ['ROLE_USER']
            },
            onEnter: ['$stateParams', '$state', '$uibModal', function($stateParams, $state, $uibModal) {
                $uibModal.open({
                    templateUrl: 'app/entities/combined/combined-dialog.html',
                    controller: 'CombinedDialogController',
                    controllerAs: 'vm',
                    backdrop: 'static',
                    size: 'lg',
                    resolve: {
                        entity: function () {
                            return {
                                name: null,
                                note: null,
                                created: null,
                                numberOfWinnersToPlayoff: null,
                                numberOfGroups: null,
                                playoffType: null,
                                inGroupTournamentType: null,
                                id: null
                            };
                        }
                    }
                }).result.then(function() {
                    $state.go('combined', null, { reload: 'combined' });
                }, function() {
                    $state.go('combined');
                });
            }]
        })
        .state('combined.edit', {
            parent: 'combined',
            url: '/{id}/edit',
            data: {
                authorities: ['ROLE_USER']
            },
            onEnter: ['$stateParams', '$state', '$uibModal', function($stateParams, $state, $uibModal) {
                $uibModal.open({
                    templateUrl: 'app/entities/combined/combined-dialog.html',
                    controller: 'CombinedDialogController',
                    controllerAs: 'vm',
                    backdrop: 'static',
                    size: 'lg',
                    resolve: {
                        entity: ['Combined', function(Combined) {
                            return Combined.get({id : $stateParams.id}).$promise;
                        }]
                    }
                }).result.then(function() {
                    $state.go('combined', null, { reload: 'combined' });
                }, function() {
                    $state.go('^');
                });
            }]
        })
        .state('combined.delete', {
            parent: 'combined',
            url: '/{id}/delete',
            data: {
                authorities: ['ROLE_USER']
            },
            onEnter: ['$stateParams', '$state', '$uibModal', function($stateParams, $state, $uibModal) {
                $uibModal.open({
                    templateUrl: 'app/entities/combined/combined-delete-dialog.html',
                    controller: 'CombinedDeleteController',
                    controllerAs: 'vm',
                    size: 'md',
                    resolve: {
                        entity: ['Combined', function(Combined) {
                            return Combined.get({id : $stateParams.id}).$promise;
                        }]
                    }
                }).result.then(function() {
                    $state.go('combined', null, { reload: 'combined' });
                }, function() {
                    $state.go('^');
                });
            }]
        });
    }

})();
