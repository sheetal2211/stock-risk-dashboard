# Issue Implementer Agent

## Purpose
Automatically scan GitHub issues and implement features for new, unassigned issues.

## Workflow

### 1. Scan for New Issues
- Fetch issues from the repository
- Filter for issues that are:
  - Status: Open
  - Not assigned to anyone
  - Created/updated within the last 24 hours

### 2. Analyze Issue Requirements
For each new issue:
- Parse the title and description
- Extract acceptance criteria
- Identify affected components
- Determine implementation complexity

### 3. Create Implementation Branch
- Create a new feature branch: `feature/issue-{number}-{slug}`
- Use conventional commit style

### 4. Implement Feature
- Update architecture diagram if needed (use `/architect` skill)
- Add code changes
- Update tests if applicable
- Follow project conventions

### 5. Create Pull Request
- Link to the original issue: `Closes #{issue_number}`
- Add implementation notes
- Request review

### 6. Track Progress
- Comment on issue with implementation status
- Update PR with any blockers

## Configuration

The agent runs on a schedule (every 6 hours) and:
1. Checks for new unimplemented issues
2. Picks the highest priority issue
3. Implements and creates a PR
4. Marks the issue as in-progress

## Tools Required
- GitHub CLI (gh) for issue/PR operations
- Git for branch management
- Standard development tools

## Environment Variables
- `GITHUB_OWNER`: sheetal2211
- `GITHUB_REPO`: stock-risk-dashboard
- `GITHUB_TOKEN`: (configured in settings)
