(function() {
    'use strict';

    angular
        .module('tournamentControlApp')
        .config(stateConfig);

    stateConfig.$inject = ['$stateProvider'];

    function stateConfig($stateProvider) {
        $stateProvider
        .state('team', {
            parent: 'entity',
            url: '/team?page&sort&search',
            data: {
                authorities: ['ROLE_USER'],
                pageTitle: 'tournamentControlApp.team.home.title'
            },
            views: {
                'content@': {
                    templateUrl: 'app/entities/team/teams.html',
                    controller: 'TeamController',
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
                    $translatePartialLoader.addPart('team');
                    $translatePartialLoader.addPart('global');
                    return $translate.refresh();
                }]
            }
        })
        .state('team-detail', {
            parent: 'team',
            url: '/team/{id}',
            data: {
                authorities: ['ROLE_USER'],
                pageTitle: 'tournamentControlApp.team.detail.title'
            },
            views: {
                'content@': {
                    templateUrl: 'app/entities/team/team-detail.html',
                    controller: 'TeamDetailController',
                    controllerAs: 'vm'
                }
            },
            resolve: {
                translatePartialLoader: ['$translate', '$translatePartialLoader', function ($translate, $translatePartialLoader) {
                    $translatePartialLoader.addPart('team');
                    return $translate.refresh();
                }],
                entity: ['$stateParams', 'Team', function($stateParams, Team) {
                    return Team.get({id : $stateParams.id}).$promise;
                }],
                previousState: ["$state", function ($state) {
                    var currentStateData = {
                        name: $state.current.name || 'team',
                        params: $state.params,
                        url: $state.href($state.current.name, $state.params)
                    };
                    return currentStateData;
                }]
            }
        })
        .state('team-detail.edit', {
            parent: 'team-detail',
            url: '/detail/edit',
            data: {
                authorities: ['ROLE_USER']
            },
            onEnter: ['$stateParams', '$state', '$uibModal', function($stateParams, $state, $uibModal) {
                $uibModal.open({
                    templateUrl: 'app/entities/team/team-dialog.html',
                    controller: 'TeamDialogController',
                    controllerAs: 'vm',
                    backdrop: 'static',
                    size: 'lg',
                    resolve: {
                        entity: ['Team', function(Team) {
                            return Team.get({id : $stateParams.id}).$promise;
                        }]
                    }
                }).result.then(function() {
                    $state.go('^', {}, { reload: false });
                }, function() {
                    $state.go('^');
                });
            }]
        })
        .state('team.new', {
            parent: 'team',
            url: '/new',
            data: {
                authorities: ['ROLE_USER']
            },
            onEnter: ['$stateParams', '$state', '$uibModal', function($stateParams, $state, $uibModal) {
                $uibModal.open({
                    templateUrl: 'app/entities/team/team-dialog.html',
                    controller: 'TeamDialogController',
                    controllerAs: 'vm',
                    backdrop: 'static',
                    size: 'lg',
                    resolve: {
                        entity: function () {
                            return {
                                name: null,
                                note: null,
                                id: null
                            };
                        }
                    }
                }).result.then(function() {
                    $state.go('team', null, { reload: 'team' });
                }, function() {
                    $state.go('team');
                });
            }]
        })
        .state('team.edit', {
            parent: 'team',
            url: '/{id}/edit',
            data: {
                authorities: ['ROLE_USER']
            },
            onEnter: ['$stateParams', '$state', '$uibModal', function($stateParams, $state, $uibModal) {
                $uibModal.open({
                    templateUrl: 'app/entities/team/team-dialog.html',
                    controller: 'TeamDialogController',
                    controllerAs: 'vm',
                    backdrop: 'static',
                    size: 'lg',
                    resolve: {
                        entity: ['Team', function(Team) {
                            return Team.get({id : $stateParams.id}).$promise;
                        }]
                    }
                }).result.then(function() {
                    $state.go('team', null, { reload: 'team' });
                }, function() {
                    $state.go('^');
                });
            }]
        })
        .state('team.delete', {
            parent: 'team',
            url: '/{id}/delete',
            data: {
                authorities: ['ROLE_USER']
            },
            onEnter: ['$stateParams', '$state', '$uibModal', function($stateParams, $state, $uibModal) {
                $uibModal.open({
                    templateUrl: 'app/entities/team/team-delete-dialog.html',
                    controller: 'TeamDeleteController',
                    controllerAs: 'vm',
                    size: 'md',
                    resolve: {
                        entity: ['Team', function(Team) {
                            return Team.get({id : $stateParams.id}).$promise;
                        }]
                    }
                }).result.then(function() {
                    $state.go('team', null, { reload: 'team' });
                }, function() {
                    $state.go('^');
                });
            }]
        });
    }

})();
