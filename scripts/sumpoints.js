
// Config

var labelGroup = "points";

// Processing

var issuesForMilestones = list(milestones).map(function (m) {
    return list(issues).filter(function (issue) {
        return issue.milestone && issue.milestone.title === m.title;
    });
});

var result = list(issuesForMilestones).map(function (issues, i) {
    var milestone = milestones[i];
    var issuesForPeople = list(users).map(function (person) {
        return {
            person: person,
            issues: list(issues).filter(function (issue) {
                return issue.assignee
                    && issue.assignee.githubName === person.githubName
                    && issue.milestone.title === milestone.title;
            })
        };
    });

    var pointsForPeople = issuesForPeople.map(function (pi) {
        return {
            person: pi.person,
            points: list(pi.issues).map(function (issue) {
                return list(issue.labels).filter(function (label) {
                    return label.group === labelGroup;
                });
            }).map(function (labels) {
                // assuming this label group is exclusive
                var label = labels[0];
                if (!isNaN(parseInt(label.name))) {
                    return parseInt(label.name);
                } else {
                    return 0;
                }
            }).reduce(function (x, y) {
                return x + y;
            }, 0)
        };
    });

    return pointsForPeople;
});

result.forEach(function (pss, i) {
    print(milestones[i] + "\n");
    pss.forEach(function (ps) {
        print(ps.person + ": " + ps.points);
    });
    print("\n");
});