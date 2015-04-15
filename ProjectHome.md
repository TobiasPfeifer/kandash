# Kanban Dashboard (Kandash) #

## Version 0.3 alfa is available ##

[![](http://dl.dropbox.com/u/7519694/kandash.jpg)](http://vimeo.com/12102858)

New in this version:

  * Reporting capabilities (cumulative flowchart, statisctics, etc.)
  * Improved interactivity with COMET support (thx to Atmosphere)
  * Boosted performance (thx to Akka)
  * Looks a bit less ugly :)
  * A lot of bugfixes

## Version 0.3 will be available soon ##

v 0.3 will be integrated with Akka Framework (code in progress is currently available under "akka" branch). Akka Framework provides functional enterprise-scale actors, simple COMET support (based on Atmosphere framework) and fault-tolerance capabilities.

## Pre-alfa version 0.1 is now available ##
Short screen-cast guide is available at [WIKI](http://code.google.com/p/kandash/wiki/Guide)

<img src='http://vasilrem.com/images/kandash.jpg' align='center' width='800'>

<h3>Why Kanban?</h3>

<ul><li>Kanban is much more comfortable and natural for maintenance teams/projects<br>
</li><li>Timeboxed iterations optional. Can have separate cadences for planning, release, and process improvement. Can be eventdriven instead of timeboxed.<br>
</li><li>Kanban limits WIP per workflow state, Scrum limits WIP per iteration (Kanban doesn't resist change within an iteration)<br>
</li><li>Commitment optional<br>
</li><li>Uses Lead time as default metric for planning and process improvement (Scrum uses velocity as default metric for planning and process improvement)<br>
</li><li>No particular item size is prescribed<br>
</li><li>Scrum board is reset between each iteration. In Kanban, the board is normally a persistent thing – you don’t need to reset it and start over<br>
</li><li>No particular type of diagram is prescribed<br>
</li><li>WIP limited directly (per workflow state)<br>
</li><li>Estimation optional<br>
</li><li>Can add new items whenever capacity is available<br>
</li><li>A kanban board may be shared by multiple teams or individuals<br>
</li><li>Prioritization is optional<br>
</li><li>Scrum prescribes crossfunctional teams. In Kanban, cross-functional teams are optional, and a board doesn’t need to be owned by one specific team. A board is related to one workflow, not necessarily one team.<br>
</li><li>Scrum backlog items must fit in a sprint. On the same Kanban board you might have one item that takes 1 month to complete and another item that takes 1 day.<br>
The list is based on the <a href='http://www.infoq.com/minibooks/kanban-scrum-minibook'>"Kanban and Scrum - making the most of both" by Henrik Kniberg and Mattias Skarin </a></li></ul>

<h3>Why Free and Open Source?</h3>

<ul><li>Because the project is mainly used as an outlet for creativity<br>
</li><li>I'm the first in the row of those, who're interested in the project to be done, because I'm using Kanban for maintenance projects, and still couldn't find anything better, than a set of Excel spreadsheets<br>
</li><li>It's easier to find like-minded persons in the open-source community</li></ul>

<h3>Why Ext JS?</h3>

Ext JS is a cross-browser JavaScript library for building rich internet applications. It includes:<br>
<br>
<ul><li>High performance, customizable UI widgets<br>
</li><li>Well designed and extensible Component model<br>
</li><li>An intuitive, easy to use API</li></ul>

With Ext JS you can build Flash-like rich user interface in the browser window, but you're free from Flash-specific pains (and, also, in makes the application Chrome-compatible)<br>
<br>
<h3>Why Scala?</h3>

Scala is what Java will look like in 2-3 next years. If you're ready to wait, go ahead and survive the tones of boilerplate code and lacking capabilities. Me not.<br>
<br>
<h3>Why Mongo?</h3>

<ul><li>Mongo is a simple and lightweight document-oriented NoSQL storage<br>
</li><li>Mongo internal syntax is based on JavaScript, so you don't have to learn a new database DSL</li></ul>

<h3>Version 0.1 (pre-alfa) features</h3>

<table><thead><th> <b>Feature</b> </th><th> <b>Progress</b> </th></thead><tbody>
<tr><td> <b>Board</b> </td><td> <b>Progress</b> </td></tr>
<tr><td> New project can be added to the board </td><td> DONE </td></tr>
<tr><td> Widths of project columns are updated when projects are added/removed/collapsed </td><td> DONE </td></tr>
<tr><td> Heights of tiers are updated when tiers removed/added/collapsed </td><td> DONE </td></tr>
<tr><td> New tier can be added to the board </td><td> DONE </td></tr>
<tr><td> New task can be added to the board </td><td> DONE </td></tr>
<tr><td> Board can be initialized with board model  </td><td> DONE </td></tr>
<tr><td> Board can be exported/imported </td><td>  </td></tr>
<tr><td> When project is collapsed, shortcut to the project is added to the project bar </td><td> DONE </td></tr>
<tr><td> AJAX calls are made to update the model on backend (CRUD tasks, projects, tiers) </td><td> DONE </td></tr>
<tr><td> Board model can be persisted on the backend </td><td> DONE </td></tr>
<tr><td> REFACTORING: Model classes are reflected on UI(controller) </td><td> DONE </td></tr>
<tr><td> Existing board can be used </td><td> DONE  </td></tr>
<tr><td> <b>Tier</b> </td><td> <b>Progress</b> </td></tr>
<tr><td> Tier can be removed from the board (all the projects) </td><td> DONE </td></tr>
<tr><td> Tier can be collapsed </td><td> DONE </td></tr>
<tr><td> WiP should be limited </td><td> DONE </td></tr>
<tr><td> Tiers can be reordered from UI </td><td> DONE </td></tr>
<tr><td> When new tier is added, other tiers are reordered </td><td> DONE </td></tr>
<tr><td> When tier is removed, all assigned tasks are removed as well </td><td> DONE </td></tr>
<tr><td> Tier can be updated </td><td> DONE </td></tr>
<tr><td> <b>Project</b> </td><td> <b>Progress</b> </td></tr>
<tr><td> Project can be removed from the board </td><td> DONE </td></tr>
<tr><td> Project can be collapsed </td><td> DONE </td></tr>
<tr><td> Project can be updated </td><td> DONE </td></tr>
<tr><td> When project is removed from the board, all tasks assigned to the project are removed as well </td><td> DONE </td></tr>
<tr><td> <b>Task</b> </td><td>  </td></tr>
<tr><td> Task can be removed from the board </td><td> DONE </td></tr>
<tr><td> Task can be updated </td><td> DONE </td></tr>
<tr><td> Task can be drag'n'dropped within one cell (tier/project) </td><td> DONE </td></tr>
<tr><td> Task can be drag'n'dropped between tiers of one project </td><td> DONE </td></tr>
<tr><td> Different types of estimation units </td><td>  </td></tr>
<tr><td> Task header is colored WRT the priority </td><td> DONE </td></tr>
<tr><td> Task can be collapsed </td><td> DONE </td></tr>
<tr><td> <b>Access control</b> </td><td> <b>Progress</b> </td></tr>
<tr><td> User model is used instead of user identifiers </td><td>  </td></tr>
<tr><td> Basic access control for C/U/D operations; restricted access to the boards </td><td>  </td></tr>
<tr><td> <b>REST-services</b> </td><td> <b>Progress</b> </td></tr>
<tr><td> Basic CRUD REST-endpoints for boards, projects, tiers and tasks </td><td> DONE </td></tr>
<tr><td> <b>Model</b> </td><td> <b>Progress</b> </td></tr>
<tr><td> Model classes are written </td><td> DONE </td></tr>
<tr><td> Model is stored to Mongo DB </td><td> DONE </td></tr>
<tr><td> API to work with boards (tasks, projects, tiers, boards CRUD) </td><td> DONE </td></tr>
<tr><td> Facts are added for task lifetime events </td><td> DONE  </td></tr>
<tr><td> <b>Reporting</b> </td><td> <b>Progress</b> </td></tr>
<tr><td> Listings of tasks in progress, finished, in the backlog </td><td>  </td></tr>
<tr><td> Cumulative flow chart </td><td>  </td></tr>
<tr><td> <b>Board selection</b> </td><td> <b>Progress</b> </td></tr>
<tr><td> List of board names are displayed </td><td> DONE </td></tr>
<tr><td> Board can be opened from the list </td><td> DONE </td></tr></tbody></table>
