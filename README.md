# redis-group-service
Common group functionality backed by a Redis cluster. Provides group membership, invites, attributes all accessible through a REST interface.

A lightweight, yet highly performant Redis cluster backed REST service supporting group functionality. Suppprted operations include

1. Group administration
  1. Creating a group
  2. Deleting a group 
2. Group membership
  1. Adding a member
  2. Deleteing a member
3. Group invitation
  1. Invite another user to a group
  2. Ask to join a group
4. Get and set attributes
  1. Group attributes
  2. Group member attributes
